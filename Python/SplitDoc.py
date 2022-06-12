#coding:utf-8

import nltk
import sys

def SplitDocument(text):  
    sen = nltk.tokenize.sent_tokenize(text)
    sen_line = ""
    for i in range(0, len(sen) - 1):
        sen_line += sen[i] + "\t"
    
    return sen_line


if __name__ == '__main__':
    a = []
    for i in range(1, len(sys.argv)):
        a.append((str(sys.argv[i])))
 
    print(SplitDocument(a[0]))
