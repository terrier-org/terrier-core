Evaluation
==========

Terrier uses [jtreceval](https://github.com/terrierteam/jtreceval) to provide the evaluation of standard retrieval runs. jtreceval contains [trec_eval](https://github.com/usnistgov/trec_eval) compiled to run under various standard platforms (e.g. Windows/Linux/Mac x86). For unsupported platforms, Terrier also includes a legacy Java evaluation package for evaluating results of TREC adhoc and named-page finding tasks.

Before running an evaluation, we need to specify the relevance assessments file in the property `trec.qrels`. To evaluate all .res result files in folder `var/results`, we can type the following:

    bin/terrier batchevaluate

The `batchevaluate` command evaluates each .res file in folder var/results using trec_eval. We can evaluate for a particular result file by giving the filename in the command line:

    bin/terrier batchevaluate -e InL2c1.0_0.res

or

    bin/trec_terrier.sh -e ./var/results/InL2c1.0_0.res

The above command evaluates only ./var/results/InL2c1.0\_0.res. For a result file named x.res, the evaluation result is saved in file x.eval, which contains the content as shown in the following example:

```
runid	all	InL2c1.0
num_q	all	93
num_ret	all	91930
num_rel	all	2083
num_rel_ret	all	1941
map	all	0.2948
gm_map	all	0.1980
Rprec	all	0.2998
bpref	all	0.9359
recip_rank	all	0.7134
iprec_at_recall_0.00	all	0.7376
iprec_at_recall_0.10	all	0.6431
iprec_at_recall_0.20	all	0.5232
iprec_at_recall_0.30	all	0.4079
iprec_at_recall_0.40	all	0.3424
iprec_at_recall_0.50	all	0.2710
iprec_at_recall_0.60	all	0.2028
iprec_at_recall_0.70	all	0.1555
iprec_at_recall_0.80	all	0.1075
iprec_at_recall_0.90	all	0.0607
iprec_at_recall_1.00	all	0.0245
P_5	all	0.4667
P_10	all	0.3677
P_15	all	0.3097
P_20	all	0.2747
P_30	all	0.2394
P_100	all	0.1285
P_200	all	0.0789
P_500	all	0.0382
P_1000	all	0.0209
```

The above displayed evaluation measures are averaged over a batch of queries. We can obtain per-query results by using option -p in the command line:

    bin/terrier batchevaluate -e PL2c1.0_0.res -p

The resulting output saved in the corresponding .eval file will contain further results, with the middle column indicating the query id.


### Using trec_eval directly

Terrier ships with [treceval](https://github.com/terrierteam/jtreceval), which contains trec\_eval binaries that work on Linux x86/x86_64, Mac OS X x86_64 and Windows x86. You can therefore use easily trec_eval directly from the command line, by invoking the `trec_eval.sh` script:

```shell

bin/terrier trec_eval /path/to/qrels var/results/PL2c1.0_0.res

```

------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
