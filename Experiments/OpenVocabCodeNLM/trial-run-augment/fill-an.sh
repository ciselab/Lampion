#!/bin/bash

# This script merges the augmented data files with the original data files. 
# It also shuffles them, as I was not sure whether the order is important for training.

# This is supposed to run on the "root" of trial-run-augment and paths only work from here

nte=./data/python-n/test_set_pre_enc_bpe_10000
ntr=./data/python-n/small_training_set_pre_enc_bpe_10000
ntv=./data/python-n/validation_set_pre_enc_bpe_10000

ate=./data/python-a/test_lampion_pre_enc_10000
atr=./data/python-a/train_lampion_pre_enc_10000
atv=./data/python-a/valid_lampion_pre_enc_10000

ante=./data/python-an/augmented_test
antr=./data/python-an/augmented_training
antv=./data/python-an/augmented_valid

cat $nte $ate | gshuf > $ante
cat $ntr $atr | gshuf > $antr
cat $ntv $atv | gshuf > $antv
