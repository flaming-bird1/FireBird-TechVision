from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from config import Config

# 创建扩展实例
db = SQLAlchemy()

# 创建Flask应用
app = Flask(__name__)

# 加载配置
app.config.from_object(Config)

# 初始化数据库
db.init_app(app)

# 导入模型类
from app.models.user import User


def _register_controllers():
    """按需导入控制器模块"""
    # 显式导入每个控制器文件
    from app.controller import predict_controller
    from app.controller import user_controller  # 如果有其他控制器
    from app.controller import json_to_shp_controller

    # 如果需要，可以在这里执行模块的初始化代码
    # 例如：predict_controller.init_module(app)


# 在应用上下文中完成初始化
with app.app_context():
    db.create_all()
    _register_controllers()  # 触发控制器注册