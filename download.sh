#!/bin/bash

# download dataset part n
n=$1
wget https://www.cs.utexas.edu/~kiat/datasets/taxi-data-sorted-large-p${n}.csv.bz2
