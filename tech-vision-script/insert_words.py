import json
import mysql.connector
import os


def import_words_from_json():
    # 数据库配置
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': '12369',
        'database': 'personal_learning_platform'
    }

    # JSON文件路径（根据你的图片结构）
    # json_file = r'E:\codefile\code-space-local\project-practice\FireBird-TechVision\tech-vision-script\output\merged_vocabulary.json'
    json_file = 'assets/output/merged_vocabulary.json'
    try:
        # 读取JSON文件
        with open(json_file, 'r', encoding='utf-8') as f:
            words_data = json.load(f)

        # 连接数据库
        connection = mysql.connector.connect(**db_config)
        cursor = connection.cursor()

        # 插入数据
        insert_query = "INSERT INTO words (word, translation) VALUES (%s, %s)"

        for word_data in words_data:
            cursor.execute(insert_query, (word_data['word'], word_data['translation']))

        connection.commit()
        print(f"成功导入 {len(words_data)} 个单词")

    except Exception as e:
        print(f"导入失败: {e}")
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()


if __name__ == "__main__":
    import_words_from_json()
