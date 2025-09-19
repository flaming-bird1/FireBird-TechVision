import json
import os
import re
from collections import defaultdict


def parse_text_file(file_path):
    """
    解析单个文本文件，提取单词和翻译

    Args:
        file_path: 文本文件路径

    Returns:
        list: 包含单词信息的字典列表
    """
    words_data = []

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            for line_num, line in enumerate(f, 1):
                line = line.strip()
                if not line:
                    continue

                # 尝试多种分隔符：制表符、空格等
                if '\t' in line:
                    parts = line.split('\t', 1)
                elif ' ' in line:
                    # 找到第一个空格的位置
                    first_space_index = line.find(' ')
                    if first_space_index != -1:
                        parts = [line[:first_space_index], line[first_space_index + 1:]]
                    else:
                        print(f"警告: 第{line_num}行格式无法解析: {line}")
                        continue
                else:
                    print(f"警告: 第{line_num}行格式无法解析: {line}")
                    continue

                word = parts[0].strip()
                translation = parts[1].strip()

                # 验证单词格式（允许连字符、撇号等）
                if not re.match(r'^[a-zA-Z\-\.\'\s]+$', word):
                    print(f"警告: 第{line_num}行单词格式异常，跳过: '{word}'")
                    continue

                # 清理单词：移除多余的空格
                word = re.sub(r'\s+', ' ', word).strip().lower()

                # 构建单词条目
                word_entry = {
                    "word": word,
                    "translation": translation
                }

                words_data.append(word_entry)

    except Exception as e:
        print(f"解析文件 {os.path.basename(file_path)} 时出错: {e}")

    return words_data


def merge_text_files(text_dir, output_file):
    """
    合并多个文本文件并去除重复单词

    Args:
        text_dir: 文本文件所在目录
        output_file: 输出JSON文件路径
    """
    # 文件列表（按你的图片中的顺序）
    file_names = [
        "1 初中-乱序.txt",
        "2 高中-乱序.txt",
        "3 四级-乱序.txt",
        "4 六级-乱序.txt",
        "5 考研-乱序.txt",
        "6 托福-乱序.txt",
        "7 SAT-乱序.txt"
    ]

    # 用于存储合并后的数据，使用字典去重
    merged_words = {}
    total_words = 0
    duplicate_count = 0

    # 逐个处理每个文件
    for file_name in file_names:
        file_path = os.path.join(text_dir, file_name)
        print(f"正在处理文件: {file_name}")

        if not os.path.exists(file_path):
            print(f"警告: 文件 {file_name} 不存在，跳过")
            continue

        try:
            words_data = parse_text_file(file_path)

            for word_data in words_data:
                total_words += 1
                word_key = word_data["word"].lower()  # 统一转为小写以避免重复

                if word_key in merged_words:
                    duplicate_count += 1
                    # 可以选择保留第一个或最后一个翻译，这里保留第一个
                    # 如果需要合并翻译，可以在这里添加逻辑
                else:
                    merged_words[word_key] = word_data

        except Exception as e:
            print(f"处理文件 {file_name} 时出错: {e}")

    # 转换为列表格式并按单词字母顺序排序
    final_data = list(merged_words.values())
    final_data.sort(key=lambda x: x["word"])

    # 创建输出目录（如果不存在）
    os.makedirs(os.path.dirname(output_file), exist_ok=True)

    # 保存到输出文件
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)

    print(f"\n合并完成!")
    print(f"总共处理单词: {total_words}")
    print(f"发现重复单词: {duplicate_count}")
    print(f"最终唯一单词数量: {len(final_data)}")
    print(f"输出文件: {output_file}")


def main():
    # 配置路径
    base_dir = os.path.dirname(os.path.abspath(__file__))
    text_dir = os.path.join(base_dir, "text")
    output_dir = os.path.join(base_dir, "output")
    output_file = os.path.join(output_dir, "merged_vocabulary.json")

    # 执行合并
    merge_text_files(text_dir, output_file)

    # 显示样本数据
    print(f"\n样本数据预览:")
    try:
        with open(output_file, 'r', encoding='utf-8') as f:
            sample_data = json.load(f)

            # 显示前5个样本
            for i in range(min(5, len(sample_data))):
                print(json.dumps(sample_data[i], ensure_ascii=False, indent=2))
                print("---")

    except Exception as e:
        print(f"读取样本数据时出错: {e}")


if __name__ == "__main__":
    main()