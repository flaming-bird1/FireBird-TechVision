<template>
  <div id="map" class="map-container"></div>
  <div class="map-controls">
    <div class="control-group">
      <button @click="zoomIn" title="放大">+</button>
      <button @click="zoomOut" title="缩小">-</button>
      <button @click="resetView" title="复位">↺</button>
      <button @click="toggleRotation" title="旋转">{{ rotationEnabled ? '锁定' : '旋转' }}</button>
    </div>
    <div class="control-group">
      <div class="mouse-position">
        <span>经度: {{ mousePosition.lon || '--' }}</span>
        <span>纬度: {{ mousePosition.lat || '--' }}</span>
      </div>
    </div>
    <div class="control-group">
      <div class="scale-control">
        比例尺: 1:{{ currentScale }}
      </div>
    </div>
    <div class="control-group layer-control">
      <h4>图层控制</h4>
      <label>
        <input type="checkbox" v-model="layers.vector.visible" @change="toggleLayer('vector')" />
        天地图矢量
      </label>
      <label>
        <input type="checkbox" v-model="layers.annotation.visible" @change="toggleLayer('annotation')" />
        天地图注记
      </label>
      <label>
        <input type="checkbox" v-model="layers.wms.visible" @change="toggleLayer('wms')" />
        GeoServer WMS
      </label>
      <div v-if="layers.wms.visible" class="opacity-control">
        <span>透明度: </span>
        <input
            type="range"
            min="0"
            max="100"
            v-model="layers.wms.opacity"
            @input="updateLayerOpacity('wms')"
        />
        <span>{{ layers.wms.opacity }}%</span>
      </div>
    </div>
  </div>
  <!-- 移除独立的overviewMap div，因为OverviewMap控件会自动创建 -->
</template>

<script setup>
import { onMounted, nextTick, ref, reactive } from 'vue'
import { Map, View } from 'ol'
import TileLayer from 'ol/layer/Tile'
import TileWMS from 'ol/source/TileWMS'
import XYZ from 'ol/source/XYZ'
import { defaults as defaultControls, Zoom, ZoomToExtent, Rotate, ScaleLine, MousePosition, OverviewMap } from 'ol/control'
import { toStringXY } from 'ol/coordinate'
import { defaults as defaultInteractions, DragPan, MouseWheelZoom, PinchZoom, KeyboardPan, KeyboardZoom } from 'ol/interaction'
import Projection from 'ol/proj/Projection'
import 'ol/ol.css'

// 天地图URL配置
const tiandituVecUrl = 'http://t0.tianditu.gov.cn/vec_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=vec&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
const tiandituCvaUrl = 'http://t0.tianditu.gov.cn/cva_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=cva&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'

// 图层状态管理
const layers = reactive({
  vector: { visible: true, layer: null },
  annotation: { visible: true, layer: null },
  wms: { visible: true, opacity: 80, layer: null }
})

// 鼠标位置
const mousePosition = reactive({
  lon: null,
  lat: null
})

// 当前比例尺
const currentScale = ref('--')

// 旋转控制
const rotationEnabled = ref(false)
let map = null
let overviewMapControl = null

// 创建天地图矢量图层
const createTiandituVecLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituVecUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.vector.visible,
    zIndex: 0
  })
  layers.vector.layer = layer
  return layer
}

// 创建天地图注记图层
const createTiandituCvaLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituCvaUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.annotation.visible,
    zIndex: 1
  })
  layers.annotation.layer = layer
  return layer
}

// 创建GeoServer WMS图层
const createGeoserverLayer = () => {
  const layer = new TileLayer({
    source: new TileWMS({
      url: '/geoserver/geoserver/wms',
      params: {
        'LAYERS': 'demo:省级行政区',
        'TILED': true,
        'FORMAT': 'image/png',
        'TRANSPARENT': true,
        'VERSION': '1.3.0'
      },
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    }),
    visible: layers.wms.visible,
    opacity: layers.wms.opacity / 100,
    zIndex: 2
  })
  layers.wms.layer = layer
  return layer
}

// 切换图层显示状态
const toggleLayer = (layerType) => {
  if (layers[layerType].layer) {
    layers[layerType].layer.setVisible(layers[layerType].visible)
  }
}

// 更新图层透明度
const updateLayerOpacity = (layerType) => {
  if (layers[layerType].layer) {
    layers[layerType].layer.setOpacity(layers[layerType].opacity / 100)
  }
}

// 地图控制函数
const zoomIn = () => {
  const view = map.getView()
  const zoom = view.getZoom()
  view.setZoom(zoom + 1)
}

const zoomOut = () => {
  const view = map.getView()
  const zoom = view.getZoom()
  view.setZoom(zoom - 1)
}

const resetView = () => {
  const view = map.getView()
  view.setCenter([116.397428, 39.90923])
  view.setZoom(4)
  view.setRotation(0)
  rotationEnabled.value = false
}

const toggleRotation = () => {
  rotationEnabled.value = !rotationEnabled.value
  const interactions = map.getInteractions()
  interactions.getArray().forEach(interaction => {
    if (interaction instanceof DragPan) {
      interaction.set('constrainRotation', rotationEnabled.value ? 0 : false)
    }
  })
}

// 初始化地图
const initMap = async () => {
  await nextTick()

  const mapContainer = document.getElementById('map')
  if (!mapContainer) {
    console.error('Map container not found')
    return
  }

  // 如果尺寸为0，添加紧急修复
  if (mapContainer.offsetWidth === 0 || mapContainer.offsetHeight === 0) {
    mapContainer.style.height = 'calc(100vh - 60px)'
    await new Promise(resolve => setTimeout(resolve, 50))
  }

  // 创建地图控件
  const zoomControl = new Zoom()
  const rotateControl = new Rotate()
  const scaleControl = new ScaleLine({
    units: 'metric',
    bar: true,
    steps: 4,
    text: true,
    minWidth: 100
  })

  const mousePositionControl = new MousePosition({
    coordinateFormat: coord => {
      if (coord) {
        mousePosition.lon = coord[0].toFixed(6)
        mousePosition.lat = coord[1].toFixed(6)
        return toStringXY(coord, 6)
      }
      return '--'
    },
    projection: 'EPSG:4326',
    className: 'custom-mouse-position'
  })

  // 创建鹰眼地图控件
  overviewMapControl = new OverviewMap({
    className: 'ol-overviewmap',
    layers: [
      new TileLayer({
        source: new XYZ({
          url: tiandituVecUrl,
          tileSize: 256,
          crossOrigin: 'anonymous',
        })
      })
    ],
    collapseLabel: '\u00BB',
    label: '\u00AB',
    collapsed: false
  })

  // 创建地图实例
  map = new Map({
    target: 'map',
    layers: [
      createTiandituVecLayer(),
      createTiandituCvaLayer(),
      createGeoserverLayer()
    ],
    view: new View({
      center: [116.397428, 39.90923],
      zoom: 4,
      projection: 'EPSG:3857',
    }),
    controls: defaultControls().extend([
      zoomControl,
      rotateControl,
      scaleControl,
      mousePositionControl,
      overviewMapControl  // 将鹰眼控件添加到地图
    ]),
    interactions: defaultInteractions().extend([
      new DragPan({ constrainRotation: false }),
      new MouseWheelZoom(),
      new PinchZoom(),
      new KeyboardPan(),
      new KeyboardZoom()
    ])
  })

  // 监听视图变化更新比例尺
  map.getView().on('change:resolution', () => {
    const resolution = map.getView().getResolution()
    const units = map.getView().getProjection().getUnits()
    const dpi = 25.4 / 0.28
    const mpu = units === 'degrees' ? 111320 : 1
    currentScale.value = Math.round(resolution * mpu * dpi * 39.37)
  })

  // 添加尺寸变化监听
  map.on('change:size', () => {
    map.updateSize()
  })

  // 确保地图尺寸正确
  setTimeout(() => {
    map.updateSize()
  }, 100)
}

onMounted(() => {
  // 添加延迟确保样式应用
  setTimeout(initMap, 100)
})
</script>

<style scoped>
.map-container {
  position: absolute;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f5f5f5;
}

.map-controls {
  position: absolute;
  top: 70px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  max-width: 250px;
}

.control-group {
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.control-group:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.control-group button {
  margin: 0 5px 5px 0;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #f8f9fa;
  cursor: pointer;
  font-weight: bold;
}

.control-group button:hover {
  background: #e9ecef;
}

.mouse-position {
  font-size: 12px;
  display: flex;
  flex-direction: column;
}

.mouse-position span {
  margin-bottom: 3px;
}

.scale-control {
  font-size: 12px;
  font-weight: bold;
  color: #333;
}

.layer-control h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}

.layer-control label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  cursor: pointer;
}

.opacity-control {
  margin-top: 8px;
  display: flex;
  align-items: center;
  font-size: 12px;
}

.opacity-control input {
  margin: 0 8px;
  width: 80px;
}

:deep(.custom-mouse-position) {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.8);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid #ccc;
}

/* 鹰眼地图样式 */
:deep(.ol-overviewmap) {
  bottom: 10px !important;
  left: auto !important;
  right: 10px !important;
  top: auto !important;
}

:deep(.ol-overviewmap .ol-overviewmap-map) {
  border: 1px solid #999;
  height: 150px;
  width: 200px;
}

:deep(.ol-overviewmap:not(.ol-collapsed)) {
  border: 2px solid #ccc;
  border-radius: 4px;
  background: white;
}

:deep(.ol-overviewmap .ol-overviewmap-box) {
  border: 2px solid rgba(0, 60, 136, 0.5);
}
</style>