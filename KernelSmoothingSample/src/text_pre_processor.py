'''
Created on Mar 11, 2012

@author: ab2283
'''

import parameters 
import os
from collections import Counter
from lowbow import lowbow_single 
import numpy as np
import logging as log
import csv
import re


log.basicConfig(level=log.DEBUG)

def preprocess_file(file_path, vocab):
    file_data_vector = []
    with open(file_path, 'r') as f:
        data = f.read()
    num_ngram = len(data) - parameters.ngram_size - 1
    for i in range(num_ngram):
        ngram = data[i:i + parameters.ngram_size]
        if ngram in vocab:
            file_data_vector.append(ngram)
    return file_data_vector

def add_ngrams_vocab(path, global_vocab):
    for label in os.listdir(path):
        label_path = os.path.join(path, label)
        local_vocab = Counter()
        for file_name in os.listdir(label_path):
            file_path = os.path.join(label_path, file_name)
            with open(file_path, 'r') as f:
                data = f.read()
            num_ngram = len(data) - parameters.ngram_size - 1
            for i in range(num_ngram):
                ngram = data[i:i + parameters.ngram_size]
                local_vocab[ngram] += 1
            log.debug('Done vocab processing for entry {0}'.format(file_name))
        local_filtered_vocab = sorted(local_vocab, key=local_vocab.get, reverse=True)[:min(parameters.ngram_per_label, len(local_vocab))]
        global_vocab.extend(local_filtered_vocab)

def build_vocab():
    global_vocab = []
    for topic in parameters.topics:
        path = os.path.join(parameters.data_root, topic)
        if os.path.exists(path):
            train_path = os.path.join(path, 'train')
            add_ngrams_vocab(train_path, global_vocab)
    return list(set(global_vocab))
        
def save_file_vector(file_path, vocab, out_file_path, header):
    log.debug("Saving vector for file {0}".format(file_path))
    with open(out_file_path, 'w+') as out_file_handle:
        out_file = csv.writer(out_file_handle)
        out_file.writerow(header)
        data = preprocess_file(file_path, vocab)
        for mu in np.arange(0, 1, 1. / (parameters.pivot_count + 1))[1:]: 
            result = lowbow_single(data, vocab, mu, parameters.c, parameters.sigma)
            out_file.writerow(list(result))
            log.debug("Got vector for mu={0}".format(mu))
    

def save_dataset(data_class, vocab):
    log.debug('starting with {0}ing data'.format(data_class))
    header = [re.sub('\s', '_', v) + str(i) for i, v in enumerate(vocab)] + ['label']
    for topic in parameters.topics:
        path = os.path.join(parameters.data_root, topic)
        out_path = os.path.join(parameters.out_root, topic)
        if os.path.exists(path):
            actual_path = os.path.join(path, data_class)
            actual_out_path = os.path.join(out_path, data_class)
            for label in os.listdir(actual_path):
                label_path = os.path.join(actual_path, label)
                label_out_path = os.path.join(actual_out_path, label)
                for file_name in os.listdir(label_path):
                    file_path = os.path.join(label_path, file_name)
                    if not os.path.exists(label_out_path):
                        os.makedirs(label_out_path)
                    file_out_path = os.path.join(label_out_path, os.path.splitext(file_name)[0] + ".csv")
                    save_file_vector(file_path, vocab, file_out_path, header)
                
    log.debug('done with {0}ing data'.format(data_class))

def get_all_ngrams(file_path):
    with open(file_path, 'r') as f:
        data = f.read()
    num_ngram = len(data) - parameters.ngram_size - 1
    return [data[i:i+parameters.ngram_size] for i in range(num_ngram)]

def save_dataset_total(data_class, vocab):
    log.debug('total: starting with {0}ing data'.format(data_class))
    csv_out_path = os.path.join(parameters.out_root, '{0}_data.csv'.format(data_class))
    file_count = len([file for root, subFolders, files in os.walk(parameters.data_root) for file in files if data_class in root])
    cnt = 1
    with open(csv_out_path, 'w+') as out_file_handle:
        out_file = csv.writer(out_file_handle)
        for topic in parameters.topics:
            path = os.path.join(parameters.data_root, topic)
            if os.path.exists(path):
                actual_path = os.path.join(path, data_class)
                for label in os.listdir(actual_path):
                    label_path = os.path.join(actual_path, label)
                    for file_name in os.listdir(label_path):
                        log.debug("Saving vectors for file {0}, {1}/{2}".format(file_name, cnt, file_count))
                        file_path = os.path.join(label_path, file_name)
                        data = preprocess_file(file_path, vocab)
                        attributes = []
                        for mu in np.arange(0, 1, 1. / (parameters.pivot_count + 1))[1:]: 
                            result = list(lowbow_single(data, vocab, mu, parameters.c, parameters.sigma))
                            attributes.extend(result)
                            log.debug("Got vector for mu={0}".format(mu))
                        out_file.writerow(attributes + [label])
                        cnt += 1
                        


def save_parameters():
    out_value = '''Parameters used for generating this data:
ngram_size = {0}
ngram_per_label = {1}
sigma = {2}
c = {3}
local_hist_discount_factor = {4}
data_root = {5}
topics = {6}
pivot_count = {7} # number of mu values to be considered
out_root = {8}
'''.format(
           parameters.ngram_size,
           parameters.ngram_per_label,
           parameters.sigma,
           parameters.c,
           parameters.local_hist_discount_factor,
           parameters.data_root,
           parameters.topics,
           parameters.pivot_count,
           parameters.out_root)
    
    if not os.path.exists(parameters.out_root):
        os.makedirs(parameters.out_root)
    
    with open(os.path.join(parameters.out_root, 'Parameters.txt'), 'w+') as param_file:
        param_file.write(out_value)
        
    log.debug('Saved parameters')

if __name__ == '__main__':
    vocab = build_vocab()
    save_parameters()
    save_dataset_total('train', vocab)
    save_dataset_total('test', vocab)
