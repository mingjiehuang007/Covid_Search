#!/usr/bin/env python
# -*- coding:utf-8 -*-
import nltk

def split_document(text):
    sen = nltk.tokenize.sent_tokenize(text)
    sen_line = ""
    for i in range(0, len(sen) - 1):
        sen_line += sen[i] + "\t"

    return sen_line

def make_dir(path):
    # 引入模块
    import os
    # 去除首位空格
    path=path.strip()
    # 去除尾部 \ 符号
    path=path.rstrip("/")
    # 判断路径是否存在
    # 存在     True
    # 不存在   False
    isExists=os.path.exists(path)
    # 判断结果
    if not isExists:
        # 如果不存在则创建目录
        # 创建目录操作函数
        os.makedirs(path)
        print(path+' 创建成功')
        return True
    else:
        # 如果目录存在则不创建，并提示目录已存在
        print(path+' 目录已存在')
        return False

def split_pdf_json():
    old_file_path = "C:/Java/EclipseWorkspace/COVIDSearch/json_to_text/pdf_json_to_text/pdf_json_to_text.txt"
    new_file_path = "C:/Java/EclipseWorkspace/COVIDSearch/data_test_nltk/json_to_text/pdf_json_to_text_nltk/pdf_json_to_text_nltk.txt"
    make_dir("C:/Java/EclipseWorkspace/COVIDSearch/data_test_nltk/json_to_text/pdf_json_to_text_nltk")
    f = open(old_file_path, encoding='UTF-8')               # 返回一个文件对象
    line = f.readline()               # 调用文件的 readline()方法
    with open(new_file_path, "w", encoding='UTF-8') as new_file:
        new_file.write(line)
    new_file.close()

    doc_list = []
    with open(new_file_path, "a", encoding='UTF-8') as new_file:
        line = f.readline()
        while line:
            doc_id = line[0 : line.index("abstract=")-1]
            abstract = line[line.index("abstract=")+10:line.index("body_text=")-1]
            body_text = line[line.index("body_text=")+11:-1]
            sen_ab = split_document(abstract)
            sen_bd = split_document(body_text)
            if len(sen_ab) == 0:
                sen_ab = "\t"
            sen_new = doc_id + "\tabstract= " + sen_ab + "body_text= " + sen_bd[:-1] + "\n"
            doc_list.append(sen_new)
            if len(doc_list) >= 10000:
                print("outputing...")
                for j in doc_list:
                    new_file.write(j)
                doc_list.clear()
                print("output over")
            line = f.readline()
        if len(doc_list) != 0:
            print("outputing...")
            for j in doc_list:
                new_file.write(j)
            doc_list.clear()
            print("output over")
    new_file.close()
    f.close()

def split_pmc_json():
    old_file_path = "C:/Java/EclipseWorkspace/COVIDSearch/json_to_text/pmc_json_to_text/pmc_json_to_text.txt"
    new_file_path = "C:/Java/EclipseWorkspace/COVIDSearch/data_test_nltk/json_to_text/pmc_json_to_text_nltk/pmc_json_to_text_nltk.txt"
    make_dir("C:/Java/EclipseWorkspace/COVIDSearch/data_test_nltk/json_to_text/pmc_json_to_text_nltk")
    f = open(old_file_path, encoding='UTF-8')               # 返回一个文件对象
    line = f.readline()               # 调用文件的 readline()方法
    with open(new_file_path, "w", encoding='UTF-8') as new_file:
        new_file.write(line)
    new_file.close()

    doc_list = []
    with open(new_file_path, "a", encoding='UTF-8') as new_file:
        line = f.readline()
        while line:
            doc_id = line[0 : line.index("body_text=")-2]
            body_text = line[line.index("body_text=")+11:-1]
            sen_bd = split_document(body_text)
            sen_new = doc_id + "\t" + "body_text= " + sen_bd[:-1] + "\n"
            doc_list.append(sen_new)
            if len(doc_list) >= 10000:
                print("outputing...")
                for j in doc_list:
                    new_file.write(j)
                doc_list.clear()
                print("output over")
            line = f.readline()
        if len(doc_list) != 0:
            print("outputing...")
            for j in doc_list:
                new_file.write(j)
            doc_list.clear()
            print("output over")
    new_file.close()
    f.close()

split_pdf_json()
split_pmc_json()