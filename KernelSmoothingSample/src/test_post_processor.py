'''
Created on Mar 12, 2012

@author: ab2283
'''

import parameters
import os
import csv


def create_single_csv(data_class):
    csv_out_path = os.path.join(parameters.out_root, '{0}_data.csv'.format(data_class))
    with open(csv_out_path, 'w+') as out_file_handle:
        out_file = csv.writer(out_file_handle)
        for topic in parameters.topics:
            path = os.path.join(parameters.out_root, topic)
            if os.path.exists(path):
                actual_path = os.path.join(path, data_class)
                for label in os.listdir(actual_path):
                    label_path = os.path.join(actual_path, label)
                    for file_name in os.listdir(label_path):
                        file_path = os.path.join(label_path, file_name)
                        attributes = []
                        with open(file_path, 'r+') as file_handle:
                            csv_file = csv.DictReader(file_handle)
                            for row in csv_file:
                                row.pop('label')
                                attributes += row.values()
                        out_file.writerow(attributes+[label])

                        
if __name__ == '__main__':
    create_single_csv('train')
    create_single_csv('test')