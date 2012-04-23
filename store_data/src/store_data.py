'''
Created on Apr 23, 2012

@author: ab2283
'''
from bs4 import BeautifulSoup
import os
import re
import pymysql

path = "Z:\\FakeBlogDetection\\ExtractBlogData\\blogDataSet"
conn = None

def process_data(text):
    new_text = re.sub(r'&lt;', "<", text)
    new_text = re.sub(r'&gt;', ">", new_text)
    new_text = re.sub(r'[\r\n]', " ", new_text)
    new_text = re.sub(r'<script.*?script>', " ", new_text)
    new_text = re.sub(r'<.*?>', " ", new_text)
    new_text = re.sub(r'&.*?;', " ", new_text)
    new_text = re.sub(r'[?]+', " ", new_text)
    return new_text

def store_in_db(row_id, text):
    global conn
    if conn is None:
        conn = pymysql.connect(host="localhost", user="root", passwd="root", db="goodblogdata")
        conn.autocommit(1)
    conn.ping()
    cur = conn.cursor()
    cur.execute("insert into data (id, text) values (%s, %s)", (row_id, text))
    

def process_file(fname):
    rx = re.compile("\w+")
    fpath = os.path.join(path, fname)
    
    def process_entries(entries):
        final_text = ""
        for e in entries:
            tokens = rx.findall(process_data(e.text))
            final_text += " ".join(tokens) + " "
        return final_text
    
    with open(fpath, "rb") as f:
        soup = BeautifulSoup(f.read())
        final_text = ""
        if soup.feed is not None:
            entries = soup.find_all("content")
            final_text = process_entries(entries)
        elif soup.channel is not None:
            entries = soup.find_all("content:encoded")
            if len(entries) == 0:
                entries = soup.find_all("atom:summary")
                
            if len(entries) == 0:
                entries = soup.find_all("description")[1:]
            
            final_text = process_entries(entries)
            
    store_in_db(fname.split(".xml")[0], final_text)
    #print final_text   

if __name__ == '__main__':
    #process_file("21416.xml")
    for fname in os.listdir(path):
        print fname
        process_file(fname)
    
