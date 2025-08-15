from flask import Flask, request, jsonify, send_file
from pathlib import Path
import json
import geopandas as gpd
import tempfile
import shutil
import zipfile
import io
from app import app


class Result:
    def __init__(self, code=200, message='success', data=None):
        self.code = code
        self.message = message
        self.data = data

    def to_dict(self):
        return {
            'code': self.code,
            'message': self.message,
            'data': self.data
        }


@app.route('/convert/json-to-shp', methods=['POST'])
def convert_json_to_shp():
    """
    将GeoJSON转换为Shapefile的API端点
    参数:
    - json_data: 直接传递的GeoJSON数据
    或
    - file: 上传的GeoJSON文件

    返回:
    - 成功: 包含Shapefile下载链接的Result对象
    - 失败: 包含错误信息的Result对象
    """
    result = Result()

    try:
        # 创建临时目录
        temp_dir = Path(tempfile.mkdtemp())
        output_dir = temp_dir / "shapefile"
        output_dir.mkdir()

        # 获取输入数据
        if 'file' in request.files:
            # 处理文件上传
            file = request.files['file']
            if not file.filename.lower().endswith('.json'):
                raise ValueError("请上传JSON文件")

            json_path = temp_dir / "input.json"
            file.save(json_path)

            with open(json_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
        elif 'json_data' in request.json:
            # 处理直接传递的JSON数据
            data = request.json['json_data']
        else:
            raise ValueError("请提供JSON文件或直接传递JSON数据")

        # 转换为GeoDataFrame
        gdf = gpd.GeoDataFrame.from_features(data['features'])

        # 设置坐标系（WGS84，EPSG:4326）
        gdf.crs = "EPSG:4326"

        # 保存为Shapefile
        output_path = output_dir / "output"
        gdf.to_file(output_path, encoding='utf-8')

        # 创建ZIP文件
        zip_path = temp_dir / "shapefile.zip"
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for file in output_dir.glob('*'):
                zipf.write(file, file.name)

        # 准备返回数据
        result.data = {
            "download_url": f"/download/{zip_path.name}",
            "shapefile_name": "output_shapefile.zip",
            "path": output_path.as_posix()
        }

    except Exception as e:
        result.code = 400
        result.message = str(e)
        # 清理临时目录
        if 'temp_dir' in locals():
            shutil.rmtree(temp_dir)
    finally:
        # 确保在成功情况下也返回结果
        return jsonify(result.to_dict())


@app.route('/download/<filename>', methods=['GET'])
def download_file(filename):
    """
    提供文件下载的端点
    """
    temp_dir = Path(tempfile.gettempdir())
    file_path = temp_dir / filename

    if not file_path.exists():
        return jsonify(Result(404, "文件不存在或已过期").to_dict())

    # 发送文件并删除临时文件
    try:
        return send_file(
            file_path,
            as_attachment=True,
            download_name=filename,
            mimetype='application/zip'
        )
    finally:
        if file_path.exists():
            file_path.unlink()
        shapefile_dir = temp_dir / filename.replace('.zip', '')
        if shapefile_dir.exists():
            shutil.rmtree(shapefile_dir)
