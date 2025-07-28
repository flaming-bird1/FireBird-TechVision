from flask import request, jsonify
from app import app, db
from app.service.mnist_service import MNISTPredictor
from app.service.cifar10_service import CIFAR10Predictor


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


# 初始化服务
mnist_predictor = MNISTPredictor(app.config['MNIST_MODEL_PATH'])
cifar10_predictor = CIFAR10Predictor(app.config['CIFAR10_MODEL_PATH'])


@app.route('/mnist/predict', methods=['POST'])
def mnist_predict():
    if 'image' not in request.files:
        return jsonify(Result(400, 'No image uploaded').to_dict())

    try:
        file = request.files['image']
        if not file.filename.lower().endswith(('.png', '.jpg', '.jpeg')):
            return jsonify(Result(400, 'Invalid image format').to_dict())

        result = mnist_predictor.predict(file.read())
        return jsonify(Result(data=result).to_dict())
    except Exception as e:
        return jsonify(Result(500, str(e)).to_dict())


@app.route('/cifar10/predict', methods=['POST'])
def cifar10_predict():
    if 'file' not in request.files:
        return jsonify(Result(400, '请上传图片文件').to_dict())

    try:
        file = request.files['file']
        if not file.filename.lower().endswith(('.png', '.jpg', '.jpeg')):
            return jsonify(Result(400, '仅支持PNG/JPG/JPEG格式').to_dict())

        result = cifar10_predictor.predict(file.read())
        return jsonify(Result(data=result).to_dict())
    except Exception as e:
        return jsonify(Result(500, f'预测失败: {str(e)}').to_dict())


# 测试数据库连接
@app.route('/test_db', methods=['GET'])
def db_test():
    try:
        db.engine.connect()  # 直接使用全局db对象
        return "MySQL 连接成功！"
    except Exception as e:
        return f"MySQL 连接失败: {str(e)}"
