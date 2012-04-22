'''
Created on Apr 21, 2012

@author: ab2283
'''
from bs4 import BeautifulSoup
import re
import urllib3
import sys

path = "Z:/FakeBlogDetection/CompressedDataset/good_blogs_list.tsv"
outpath = "Z:/FakeBlogDetection/CompressedDataset/good_blog_feeds/good_blogs_feed_list_{0}_{1}.txt"

def get_rss_feed_url(data, main_url):
    feed_urls = []
    feed_url_lower = []
    for line in data.split("\n"):
        m = re.match(r'.*?href="(.*?)".*?', line)
        if m is not None:
            url = m.group(1)
            if url is not None:
                url = url.split("?")[0]
                url_l = url.lower()
                if "feed" in url_l or "atom" in url_l or "rss" in url_l:
                    feed_urls.append(url)
                    feed_url_lower.append(url_l)
                   
    
    feed_conditions = [["feeds.feedburner.com" in feed for feed in feed_url_lower],
                  [feed.endswith("feeds/posts/default") for feed in feed_url_lower],
                  [feed.endswith("feed/") or feed.endswith("feed") for feed in feed_url_lower],
                  [feed.endswith("rss") for feed in feed_url_lower],
                  ["feed" in feed for feed in feed_url_lower],
                  ["rss" in feed for feed in feed_url_lower],
                  ["xml" in feed for feed in feed_url_lower]]
    
    for conditions in feed_conditions:
        for condition, feed in zip(conditions, feed_urls):
            if condition:
                return [main_url, feed]
    
    return [main_url]
    
if __name__ == '__main__':
    #TODO: typepad blogs need to be handled seperately
    
    with open(path) as f:
        lines = [ line.split("\t")[1] for line in f.readlines() if len(line) > 0 and not line.startswith("#") and len(line.split("\t")) == 3]
    
    start_index = int(sys.argv[1])
    count = int(sys.argv[2])
    end_index = start_index + count
    outpath = outpath.format(start_index, count)
    length = len(lines)
    http = urllib3.PoolManager()
    with open(outpath, "w") as outfile:
        for i, url in enumerate(lines[start_index:end_index]):
            print i, "/", length, url
            try:
                response = http.request('GET', url)
            except:
                continue
            
            flag = False
            try:
                soup = BeautifulSoup(response.data)
                rss_tag = soup.find(type='application/rss+xml')
                if rss_tag is not None:
                    feed = rss_tag.get('href')
                    feed = [url, feed]
                    flag = True
            except:
                flag = False
            
            if flag == False:
                feed = get_rss_feed_url(response.data, url)
    
            if len(feed) != 2:
                continue
            
            outfile.write(feed[0] + "\t" + feed[1] + "\n")
            outfile.flush();
        
