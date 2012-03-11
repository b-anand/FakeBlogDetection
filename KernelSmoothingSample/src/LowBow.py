'''
Created on Mar 11, 2012

@author: ab2283
'''

from scipy.stats import norm
from scipy import integrate
import matplotlib.pyplot as plt
import numpy as np
import math

def bounded_gaussian_kernel(vals, mu, sigma):
    mu *= 1.
    sigma *= 1.
    return [norm(loc=mu, scale=sigma).pdf(x) / (norm.cdf((1 - mu) / sigma) - norm.cdf((-mu) / sigma)) if x >= 0 and x <= 1 else 0 for x in vals]

def simple_step(x, j, data, vocab, c, mu, sigma):
    n = len(data)
    v = len(vocab)
    i = int(math.floor(x * n))
    value = (1 + c) / (1 + c * v) if data[i] == vocab[j] else c / (1 + c * v) 
    value = value * bounded_gaussian_kernel([x], mu, sigma)[0]
    return value

def lowbow_test():
    data = [1, 1, 1, 2, 2, 1, 1, 1, 2, 1, 1]
    vocab = list(set(data))
    c = 0.005
    sigma = 0.1
    step = 0.01
    j = 0
    values = []
    steps = np.arange(0, 1, step)
    for mu in steps:
        args = (j, data, vocab, c, mu, sigma)
        results = integrate.quad(simple_step, 0, 1, args); 
        values.append(results[0])
        print mu
    plt.plot(steps, values)
    plt.show()

if __name__ == '__main__':
    lowbow_test()
