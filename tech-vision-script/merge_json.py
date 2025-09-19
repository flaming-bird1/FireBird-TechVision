import json
import os
from collections import defaultdict


def merge_json_files(file_list, output_file):
    """
    合并多个JSON文件并去除重复内容

    Args:
        file_list: 要合并的JSON文件列表（包含完整路径）
        output_file: 输出文件名
    """

    # 用于存储合并后的数据，按单词分组
    merged_data = defaultdict(lambda: {
        "translations": [],
        "phrases": []
    })

    # 处理重复计数
    duplicate_count = 0
    total_words = 0

    # 逐个读取并处理每个文件
    for file_path in file_list:
        print(f"正在处理文件: {os.path.basename(file_path)}")

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)

            for item in data:
                total_words += 1
                word = item["word"].lower()  # 统一转为小写以避免大小写重复

                # 如果单词已存在，合并数据
                if word in merged_data:
                    duplicate_count += 1

                    # 合并翻译
                    existing_translations = {t["translation"] for t in merged_data[word]["translations"]}
                    for translation in item.get("translations", []):
                        if translation["translation"] not in existing_translations:
                            merged_data[word]["translations"].append(translation)
                            existing_translations.add(translation["translation"])

                    # 合并短语
                    existing_phrases = {p["phrase"] for p in merged_data[word].get("phrases", [])}
                    for phrase in item.get("phrases", []):
                        if phrase["phrase"] not in existing_phrases:
                            merged_data[word]["phrases"].append(phrase)
                            existing_phrases.add(phrase["phrase"])
                else:
                    # 新单词，直接添加
                    merged_data[word] = {
                        "translations": item.get("translations", []),
                        "phrases": item.get("phrases", [])
                    }

        except Exception as e:
            print(f"处理文件 {os.path.basename(file_path)} 时出错: {e}")

    # 转换为最终的输出格式
    final_data = []
    for word, content in merged_data.items():
        word_entry = {"word": word}
        if content["translations"]:
            word_entry["translations"] = content["translations"]
        if content["phrases"]:
            word_entry["phrases"] = content["phrases"]
        final_data.append(word_entry)

    # 按单词字母顺序排序
    final_data.sort(key=lambda x: x["word"])

    # 保存到输出文件
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(final_data, f, ensure_ascii=False, indent=2)

    print(f"\n合并完成!")
    print(f"总共处理单词: {total_words}")
    print(f"发现重复单词: {duplicate_count}")
    print(f"最终唯一单词数量: {len(final_data)}")
    print(f"输出文件: {output_file}")


def main():
    # JSON文件所在的目录
    json_dir = "json"

    # 文件列表
    file_names = [
        "1-初中-顺序.json",
        "2-高中-顺序.json",
        "3-CET4-顺序.json",
        "4-CET6-顺序.json",
        "5-考研-顺序.json",
        "6-托福-顺序.json",
        "7-SAT-顺序.json"
    ]

    # 构建完整文件路径
    file_list = [os.path.join(json_dir, file_name) for file_name in file_names]

    # 检查文件是否存在
    existing_files = []
    for file_path in file_list:
        if os.path.exists(file_path):
            existing_files.append(file_path)
        else:
            print(f"警告: 文件 {file_path} 不存在")

    if not existing_files:
        print("没有找到任何JSON文件!")
        return

    # 输出文件名（保存在当前目录）
    output_file = "output/merged_vocabulary_json.json"

    # 执行合并
    merge_json_files(existing_files, output_file)


if __name__ == "__main__":
    main()