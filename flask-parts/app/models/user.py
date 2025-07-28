from app import db


class User(db.Model):
    """用户模型类"""
    __tablename__ = 'users'

    user_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(50), nullable=False, unique=True)
    password = db.Column(db.String(20), nullable=False)
    avatar_url = db.Column(db.String(255))
    title = db.Column(db.String(100))
    bio = db.Column(db.Text)
    email = db.Column(db.String(100))
    github_url = db.Column(db.String(255))
    blog_url = db.Column(db.String(255))
    created_at = db.Column(db.TIMESTAMP, server_default=db.func.current_timestamp())
    updated_at = db.Column(db.TIMESTAMP, server_default=db.func.current_timestamp(),
                           onupdate=db.func.current_timestamp())

    def to_dict(self):
        """将对象转换为字典"""
        return {
            'user_id': self.user_id,
            'username': self.username,
            'avatar_url': self.avatar_url,
            'email': self.email
        }
