'''
Created on Mar 11, 2012

@author: ab2283
'''

ngram_size = 3
ngram_per_label = 30 #TODO redundant for now
sigma = 0.2
c = 0.005
local_hist_discount_factor = 0.6 #TODO redundant for now
data_root = r'Z:\FakeBlogDetectionData\Dataset'
topics = ['authorship_poetry', 'rawdata_cric', 'rawdata_fin', 'rawdata_nfl', 'rawdata_travel']
pivot_count = 5 # number of mu values to be considered
out_root = r'Z:\FakeBlogDetectionData\ProcessedDataset_' + str(pivot_count) + '_' + str(sigma)