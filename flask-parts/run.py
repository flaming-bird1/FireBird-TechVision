from app import app  # 从 app/__init__.py 文件中导入 app 实例

# # 方案B（工厂函数模式）
# from app import create_app
# app = create_app()

if __name__ == '__main__':
    app.run(port=5000)
