<template>
  <div id="map" class="map-container"></div>
</template>

<script setup>
import {onMounted, nextTick} from 'vue'
import {Map, View} from 'ol'
import TileLayer from 'ol/layer/Tile'
import TileWMS from 'ol/source/TileWMS'
import OSM from 'ol/source/OSM'  // 导入 OpenStreetMap 的底图数据源
import {defaults as defaultControls} from 'ol/control'
import 'ol/ol.css'

// url: 'http://localhost:8080/geoserver/wms',

// 创建图层
const geoserverLayer = new TileLayer({
  source: new TileWMS({
    url: '/geoserver/geoserver/wms', // 使用代理路径
    params: {
      'LAYERS': 'demo:省级行政区', // 工作区:图层名
      'TILED': true,               // 启用瓦片
      'FORMAT': 'image/png',       // 图片格式
      'TRANSPARENT': true,         // 透明背景
      'VERSION': '1.3.0'          // WMS版本
    },
    serverType: 'geoserver',      // 服务类型
    crossOrigin: 'anonymous'      // 跨域处理
  }),
  visible: true,                  // 图层可见
  opacity: 0.8                    // 透明度80%
})

const initMap = async () => {
  await nextTick()

  const mapContainer = document.getElementById('map')
  if (!mapContainer) {
    console.error('Map container not found')
    return
  }

  // 调试输出容器尺寸
  console.log('Container size:', mapContainer.offsetWidth, mapContainer.offsetHeight)

  // 如果尺寸为0，添加紧急修复
  if (mapContainer.offsetWidth === 0 || mapContainer.offsetHeight === 0) {
    mapContainer.style.height = 'calc(100vh - 60px)'
    await new Promise(resolve => setTimeout(resolve, 50))
  }

  // 创建地图实例
  const map = new Map({
    target: 'map',
    layers: [geoserverLayer],//osmLayer
    view: new View({
      center: [116.4, 39.9],
      zoom: 4,
      projection: 'EPSG:4326'
    }),
    controls: defaultControls({
      zoom: true,
      rotate: false,
      attribution: true
    })
  })

  // 添加尺寸变化监听
  map.on('change:size', () => {
    map.updateSize()
  })

  // 确保地图尺寸正确
  setTimeout(() => {
    map.updateSize()
    console.log('Map updated size')
  }, 100)
}

onMounted(() => {
  // 添加100ms延迟确保样式应用
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
</style>