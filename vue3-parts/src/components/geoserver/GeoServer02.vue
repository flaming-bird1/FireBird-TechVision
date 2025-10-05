<template>
  <div class="app-container">
    <!-- 侧边控制面板 -->
    <div class="control-panel" :class="{ collapsed: panelCollapsed }">
      <div class="panel-header">
        <h2>地图控制面板</h2>
        <button class="toggle-panel" @click="panelCollapsed = !panelCollapsed">
          {{ panelCollapsed ? '◀' : '▶' }}
        </button>
      </div>

      <div class="panel-content">
        <!-- 底图选择 -->
        <div class="control-section">
          <h3 class="section-title">底图选择</h3>
          <div class="base-layer-selector">
            <div
                class="layer-option"
                :class="{ active: baseLayer === 'vector' }"
                @click="baseLayer = 'vector'"
            >
              <div class="layer-thumb vector-thumb"></div>
              <span>天地图矢量</span>
            </div>
            <div
                class="layer-option"
                :class="{ active: baseLayer === 'image' }"
                @click="baseLayer = 'image'"
            >
              <div class="layer-thumb image-thumb"></div>
              <span>天地图影像</span>
            </div>
            <div
                class="layer-option"
                :class="{ active: baseLayer === 'terrain' }"
                @click="baseLayer = 'terrain'"
            >
              <div class="layer-thumb terrain-thumb"></div>
              <span>地形晕渲</span>
            </div>
          </div>
        </div>

        <!-- 叠加图层 -->
        <div class="control-section">
          <h3 class="section-title">叠加图层</h3>
          <div class="layer-list">
            <div class="layer-item">
              <label class="checkbox-container">
                <input type="checkbox" v-model="layers.annotation.visible" @change="toggleLayer('annotation')" />
                <span class="checkmark"></span>
                天地图注记
              </label>
            </div>
            <div class="layer-item">
              <label class="checkbox-container">
                <input type="checkbox" v-model="layers.terrainAnnotation.visible" @change="toggleLayer('terrainAnnotation')" />
                <span class="checkmark"></span>
                地形注记
              </label>
            </div>
            <div class="layer-item">
              <label class="checkbox-container">
                <input type="checkbox" v-model="layers.wms.visible" @change="toggleLayer('wms')" />
                <span class="checkmark"></span>
                省级行政区
              </label>
              <div v-if="layers.wms.visible" class="opacity-control">
                <span class="opacity-label">透明度</span>
                <input
                    type="range"
                    min="0"
                    max="100"
                    v-model="layers.wms.opacity"
                    @input="updateLayerOpacity('wms')"
                    class="slider"
                />
                <span class="opacity-value">{{ layers.wms.opacity }}%</span>
              </div>
            </div>
            <div class="layer-item">
              <label class="checkbox-container">
                <input type="checkbox" v-model="layers.cities.visible" @change="toggleLayer('cities')" />
                <span class="checkmark"></span>
                城市数据
              </label>
              <div v-if="layers.cities.visible" class="opacity-control">
                <span class="opacity-label">透明度</span>
                <input
                    type="range"
                    min="0"
                    max="100"
                    v-model="layers.cities.opacity"
                    @input="updateLayerOpacity('cities')"
                    class="slider"
                />
                <span class="opacity-value">{{ layers.cities.opacity }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 测量工具 -->
        <div class="control-section">
          <h3 class="section-title">测量工具</h3>
          <div class="measurement-controls">
            <button
                @click="toggleMeasurement('distance')"
                :class="{ active: activeMeasurement === 'distance' }"
                class="measurement-btn"
            >
              <span class="icon">⎯</span>
              距离测量
            </button>
            <button
                @click="toggleMeasurement('area')"
                :class="{ active: activeMeasurement === 'area' }"
                class="measurement-btn"
            >
              <span class="icon">□</span>
              面积测量
            </button>
            <button @click="clearMeasurements" class="clear-btn">
              <span class="icon">×</span>
              清除
            </button>
          </div>
          <div v-if="measurementResult" class="measurement-result">
            <p>{{ measurementResult }}</p>
          </div>
        </div>

        <!-- 地图信息 -->
        <div class="control-section">
          <h3 class="section-title">地图信息</h3>
          <div class="map-info">
            <div class="info-item">
              <span class="info-label">经度:</span>
              <span class="info-value">{{ mousePosition.lon || '--' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">纬度:</span>
              <span class="info-value">{{ mousePosition.lat || '--' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">比例尺:</span>
              <span class="info-value">1:{{ currentScale }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地图容器 -->
    <div id="map" class="map-container"></div>

    <!-- 地图控制按钮 -->
    <div class="map-controls">
      <button @click="zoomIn" class="control-btn" title="放大">
        <span class="icon">+</span>
      </button>
      <button @click="zoomOut" class="control-btn" title="缩小">
        <span class="icon">-</span>
      </button>
      <button @click="resetView" class="control-btn" title="复位">
        <span class="icon">↺</span>
      </button>
      <button @click="toggleRotation" class="control-btn" :title="rotationEnabled ? '锁定方向' : '允许旋转'">
        <span class="icon">{{ rotationEnabled ? '🔒' : '🔄' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, nextTick, ref, reactive, watch } from 'vue'
import { Map, View } from 'ol'
import TileLayer from 'ol/layer/Tile'
import TileWMS from 'ol/source/TileWMS'
import XYZ from 'ol/source/XYZ'
import { defaults as defaultControls, Zoom, ZoomToExtent, Rotate, ScaleLine, MousePosition, OverviewMap } from 'ol/control'
import { toStringXY } from 'ol/coordinate'
import { defaults as defaultInteractions, DragPan, MouseWheelZoom, PinchZoom, KeyboardPan, KeyboardZoom } from 'ol/interaction'
import Projection from 'ol/proj/Projection'
import VectorLayer from 'ol/layer/Vector'
import VectorSource from 'ol/source/Vector'
import { Draw, Modify, Snap } from 'ol/interaction'
import { LineString, Polygon } from 'ol/geom'
import { getLength, getArea } from 'ol/sphere'
import { Style, Stroke, Fill, Circle } from 'ol/style'
import 'ol/ol.css'

// 天地图URL配置
const tiandituVecUrl = 'http://t0.tianditu.gov.cn/vec_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=vec&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
const tiandituCvaUrl = 'http://t0.tianditu.gov.cn/cva_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=cva&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
const tiandituImgUrl = 'http://t0.tianditu.gov.cn/img_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=img&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
const tiandituImaUrl = 'http://t0.tianditu.gov.cn/cia_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=cia&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
// 地形图层URL
const tiandituTerrainUrl = 'http://t0.tianditu.gov.cn/ter_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=ter&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'
const tiandituTerrainAnnotationUrl = 'http://t0.tianditu.gov.cn/cta_w/wmts?tk=a4a9b96f495e689019f110499c74f85b&layer=cta&style=default&tilematrixset=w&Service=WMTS&Request=GetTile&Version=1.0.0&Format=tiles&TileMatrix={z}&TileCol={x}&TileRow={y}'

// 图层状态管理
const layers = reactive({
  vector: { visible: true, layer: null },
  image: { visible: false, layer: null },
  terrain: { visible: false, layer: null },
  annotation: { visible: true, layer: null },
  imageAnnotation: { visible: false, layer: null },
  terrainAnnotation: { visible: true, layer: null },
  wms: { visible: true, opacity: 80, layer: null },
  cities: { visible: true, opacity: 80, layer: null }
})

// 底图选择
const baseLayer = ref('vector')

// 鼠标位置
const mousePosition = reactive({
  lon: null,
  lat: null
})

// 当前比例尺
const currentScale = ref('--')

// 旋转控制
const rotationEnabled = ref(false)

// 测量相关
const activeMeasurement = ref(null)
const measurementResult = ref(null)

// 控制面板状态
const panelCollapsed = ref(false)

let map = null
let overviewMapControl = null
let drawInteraction = null
let modifyInteraction = null
let snapInteraction = null
let vectorSource = null
let vectorLayer = null

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

// 创建天地图影像图层
const createTiandituImgLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituImgUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.image.visible,
    zIndex: 0
  })
  layers.image.layer = layer
  return layer
}

// 创建天地图地形晕渲图层
const createTiandituTerrainLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituTerrainUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.terrain.visible,
    zIndex: 0
  })
  layers.terrain.layer = layer
  return layer
}

// 创建天地图矢量注记图层
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

// 创建天地图影像注记图层
const createTiandituImaLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituImaUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.imageAnnotation.visible,
    zIndex: 1
  })
  layers.imageAnnotation.layer = layer
  return layer
}

// 创建天地图地形注记图层
const createTiandituTerrainAnnotationLayer = () => {
  const layer = new TileLayer({
    source: new XYZ({
      url: tiandituTerrainAnnotationUrl,
      tileSize: 256,
      crossOrigin: 'anonymous',
    }),
    visible: layers.terrainAnnotation.visible,
    zIndex: 1
  })
  layers.terrainAnnotation.layer = layer
  return layer
}

// 创建GeoServer WMS图层 - 省级行政区
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

// 创建GeoServer WMS图层 - 城市数据
const createCitiesLayer = () => {
  const layer = new TileLayer({
    source: new TileWMS({
      url: '/geoserver/geoserver/wms',
      params: {
        'LAYERS': 'demo:cities',
        'TILED': true,
        'FORMAT': 'image/png',
        'TRANSPARENT': true,
        'VERSION': '1.3.0'
      },
      serverType: 'geoserver',
      crossOrigin: 'anonymous'
    }),
    visible: layers.cities.visible,
    opacity: layers.cities.opacity / 100,
    zIndex: 3
  })
  layers.cities.layer = layer
  return layer
}

// 创建测量图层
const createMeasurementLayer = () => {
  vectorSource = new VectorSource()

  vectorLayer = new VectorLayer({
    source: vectorSource,
    style: new Style({
      fill: new Fill({
        color: 'rgba(255, 255, 0, 0.2)'
      }),
      stroke: new Stroke({
        color: '#ffcc33',
        width: 2
      }),
      image: new Circle({
        radius: 7,
        fill: new Fill({
          color: '#ffcc33'
        })
      })
    }),
    zIndex: 100
  })

  return vectorLayer
}

// 切换底图
watch(baseLayer, (newValue) => {
  if (newValue === 'vector') {
    layers.vector.layer.setVisible(true)
    layers.image.layer.setVisible(false)
    layers.terrain.layer.setVisible(false)
    layers.annotation.layer.setVisible(true)
    layers.imageAnnotation.layer.setVisible(false)
    layers.terrainAnnotation.layer.setVisible(false)
  } else if (newValue === 'image') {
    layers.vector.layer.setVisible(false)
    layers.image.layer.setVisible(true)
    layers.terrain.layer.setVisible(false)
    layers.annotation.layer.setVisible(false)
    layers.imageAnnotation.layer.setVisible(true)
    layers.terrainAnnotation.layer.setVisible(false)
  } else if (newValue === 'terrain') {
    layers.vector.layer.setVisible(false)
    layers.image.layer.setVisible(false)
    layers.terrain.layer.setVisible(true)
    layers.annotation.layer.setVisible(false)
    layers.imageAnnotation.layer.setVisible(false)
    layers.terrainAnnotation.layer.setVisible(true)
  }
})

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

// 测量功能
const toggleMeasurement = (type) => {
  if (activeMeasurement.value === type) {
    clearMeasurements()
    return
  }

  clearMeasurements()
  activeMeasurement.value = type

  let geometryType
  if (type === 'distance') {
    geometryType = 'LineString'
  } else if (type === 'area') {
    geometryType = 'Polygon'
  }

  drawInteraction = new Draw({
    source: vectorSource,
    type: geometryType,
    style: new Style({
      fill: new Fill({
        color: 'rgba(255, 255, 0, 0.2)'
      }),
      stroke: new Stroke({
        color: '#ffcc33',
        width: 2
      }),
      image: new Circle({
        radius: 7,
        fill: new Fill({
          color: '#ffcc33'
        })
      })
    })
  })

  drawInteraction.on('drawend', (event) => {
    const geometry = event.feature.getGeometry()
    calculateMeasurement(geometry)

    modifyInteraction = new Modify({ source: vectorSource })
    modifyInteraction.on('modifyend', (event) => {
      const features = event.features.getArray()
      if (features.length > 0) {
        calculateMeasurement(features[0].getGeometry())
      }
    })

    map.addInteraction(modifyInteraction)
    snapInteraction = new Snap({ source: vectorSource })
    map.addInteraction(snapInteraction)
  })

  map.addInteraction(drawInteraction)
}

// 计算测量结果
const calculateMeasurement = (geometry) => {
  if (geometry instanceof LineString) {
    const length = getLength(geometry)
    if (length > 1000) {
      measurementResult.value = (length / 1000).toFixed(2) + ' 公里'
    } else {
      measurementResult.value = length.toFixed(2) + ' 米'
    }
  } else if (geometry instanceof Polygon) {
    const area = getArea(geometry)
    if (area > 1000000) {
      measurementResult.value = (area / 1000000).toFixed(2) + ' 平方公里'
    } else {
      measurementResult.value = area.toFixed(2) + ' 平方米'
    }
  }
}

// 清除测量
const clearMeasurements = () => {
  if (drawInteraction) {
    map.removeInteraction(drawInteraction)
    drawInteraction = null
  }

  if (modifyInteraction) {
    map.removeInteraction(modifyInteraction)
    modifyInteraction = null
  }

  if (snapInteraction) {
    map.removeInteraction(snapInteraction)
    snapInteraction = null
  }

  if (vectorSource) {
    vectorSource.clear()
  }

  activeMeasurement.value = null
  measurementResult.value = null
}

// 初始化地图
const initMap = async () => {
  await nextTick()

  const mapContainer = document.getElementById('map')
  if (!mapContainer) {
    console.error('Map container not found')
    return
  }

  if (mapContainer.offsetWidth === 0 || mapContainer.offsetHeight === 0) {
    mapContainer.style.height = '100vh'
    await new Promise(resolve => setTimeout(resolve, 50))
  }

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

  map = new Map({
    target: 'map',
    layers: [
      createTiandituVecLayer(),
      createTiandituImgLayer(),
      createTiandituTerrainLayer(),
      createTiandituCvaLayer(),
      createTiandituImaLayer(),
      createTiandituTerrainAnnotationLayer(),
      createGeoserverLayer(),
      createCitiesLayer(),
      createMeasurementLayer()
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
      overviewMapControl
    ]),
    interactions: defaultInteractions().extend([
      new DragPan({ constrainRotation: false }),
      new MouseWheelZoom(),
      new PinchZoom(),
      new KeyboardPan(),
      new KeyboardZoom()
    ])
  })

  map.getView().on('change:resolution', () => {
    const resolution = map.getView().getResolution()
    const units = map.getView().getProjection().getUnits()
    const dpi = 25.4 / 0.28
    const mpu = units === 'degrees' ? 111320 : 1
    currentScale.value = Math.round(resolution * mpu * dpi * 39.37)
  })

  map.on('change:size', () => {
    map.updateSize()
  })

  setTimeout(() => {
    map.updateSize()
  }, 100)
}

onMounted(() => {
  setTimeout(initMap, 100)
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.map-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f5f5f5;
}

/* 控制面板样式 */
.control-panel {
  position: absolute;
  top: 0;
  left: 0;
  width: 320px;
  height: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.control-panel.collapsed {
  transform: translateX(-100%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #2c3e50, #4a6491);
  color: white;
}

.panel-header h2 {
  font-size: 1.3rem;
  font-weight: 600;
}

.toggle-panel {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.toggle-panel:hover {
  background: rgba(255, 255, 255, 0.3);
}

.panel-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.control-section {
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eaeaea;
  font-weight: 600;
}

/* 底图选择器样式 */
.base-layer-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.layer-option {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.layer-option:hover {
  border-color: #a0a0a0;
}

.layer-option.active {
  border-color: #3498db;
  background-color: #f0f8ff;
}

.layer-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 12px;
  border: 1px solid #ddd;
}

.vector-thumb {
  background: linear-gradient(45deg, #e0e0e0, #f5f5f5);
}

.image-thumb {
  background: linear-gradient(45deg, #87CEEB, #98FB98);
}

.terrain-thumb {
  background: linear-gradient(45deg, #8B4513, #D2B48C);
}

/* 图层列表样式 */
.layer-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.layer-item {
  padding: 10px;
  border-radius: 6px;
  background: #f9f9f9;
}

/* 自定义复选框样式 */
.checkbox-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 0.95rem;
}

.checkbox-container input {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #ccc;
  border-radius: 3px;
  margin-right: 10px;
  position: relative;
  transition: all 0.2s;
}

.checkbox-container input:checked + .checkmark {
  background-color: #3498db;
  border-color: #3498db;
}

.checkbox-container input:checked + .checkmark:after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

/* 透明度控制 */
.opacity-control {
  display: flex;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e0e0e0;
}

.opacity-label {
  font-size: 0.85rem;
  color: #666;
  margin-right: 10px;
  min-width: 50px;
}

.slider {
  flex: 1;
  height: 5px;
  background: #e0e0e0;
  border-radius: 5px;
  outline: none;
  -webkit-appearance: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #3498db;
  cursor: pointer;
}

.opacity-value {
  font-size: 0.85rem;
  color: #3498db;
  font-weight: 600;
  min-width: 40px;
  text-align: right;
}

/* 测量工具样式 */
.measurement-controls {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.measurement-btn, .clear-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.2s;
}

.measurement-btn {
  background: #f0f0f0;
  color: #333;
}

.measurement-btn:hover {
  background: #e0e0e0;
}

.measurement-btn.active {
  background: #3498db;
  color: white;
}

.clear-btn {
  background: #e74c3c;
  color: white;
  max-width: 70px;
}

.clear-btn:hover {
  background: #c0392b;
}

.measurement-result {
  background: #e8f4fd;
  padding: 10px;
  border-radius: 6px;
  text-align: center;
  font-weight: 600;
  color: #2980b9;
}

/* 地图信息样式 */
.map-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.info-label {
  font-size: 0.9rem;
  color: #666;
}

.info-value {
  font-size: 0.9rem;
  font-weight: 600;
  color: #2c3e50;
}

/* 地图控制按钮 */
.map-controls {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 1000;
}

.control-btn {
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: all 0.2s;
}

.control-btn:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

/* 图标样式 */
.icon {
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .control-panel {
    width: 280px;
  }

  .panel-header h2 {
    font-size: 1.1rem;
  }

  .base-layer-selector {
    gap: 8px;
  }

  .layer-option {
    padding: 10px;
  }
}

/* 滚动条样式 */
.control-panel::-webkit-scrollbar {
  width: 6px;
}

.control-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.control-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.control-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* OpenLayers 控件样式覆盖 */
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