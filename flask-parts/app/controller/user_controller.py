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


@app.route('/register', methods=['POST'])
def register():
    # 获取请求数据
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    # 基本验证
    if not username or not password:
        return jsonify(Result(400, '用户名和密码不能为空').to_dict())

    if len(password) < 6:
        return jsonify(Result(400, '密码长度不能少于6位').to_dict())

        # 检查用户名是否已存在
    if User.query.filter_by(username=username).first():
        return jsonify(Result(400, '用户名已存在').to_dict())

    # 创建新用户
    try:
        new_user = User(
            username=username,
            password=password  # 注意：实际项目中应该加密存储
        )
        db.session.add(new_user)
        db.session.commit()

        return jsonify(Result(data={
            'user_id': new_user.user_id,
            'username': new_user.username
        }).to_dict())

    except Exception as e:
        db.session.rollback()
        return jsonify(Result(500, f'注册失败: {str(e)}').to_dict())
