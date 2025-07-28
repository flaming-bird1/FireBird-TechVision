from flask import request, jsonify
from app import app, db
from app.models.user import User


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


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify(Result(400, '用户名和密码不能为空').to_dict())

    user = User.query.filter_by(username=username).first()

    if not user:
        return jsonify(Result(404, '用户不存在').to_dict())

    if user.password != password:
        return jsonify(Result(401, '密码错误').to_dict())

    # 登录成功，返回用户基本信息
    return jsonify(Result(data=user.to_dict()).to_dict())