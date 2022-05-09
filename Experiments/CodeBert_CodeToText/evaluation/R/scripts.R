if (!require(rstatix)){ install.packages("rstatix") }
if (!require(effsize)){ install.packages("effsize") }
if (!require(PMCMR)){ install.packages("PMCMR") }
if (!require(tsutils)){ install.packages("tsutils") }
if (!require(permuco)){ install.packages("permuco") }

## read csv file
full.results <- read.csv("bleus.csv")

for (language in unique(full.results$prefix)){
  print(paste("## RESULTS FOR", language))
  
  results <- full.results[full.results$prefix==language, ]
  # reference results
  results.base <- results[results$config=="reference", ]
  
  ## RQ2
  print("## RQ1") 
  # 1. Results for first-order transformations
  results.changed <- results[results$transformations=="1", ]
  
  # Statistical significance for RQ1 considering all data values
  p <- wilcox.test(results.base$bleu_score, results.changed$bleu_score, alternative="two.sided", paired=F)
  print("Statistical significance for BLEU-Score considering the entire test set") 
  print(p)
  
  
  print("Statistical significance for BLEU-Score considering the code snippets with changes") 
  results.changed <- results[results$transformations=="1" & results$different_to_ref=="True", ]
  p <- wilcox.test(results.base$bleu_score, results.changed$bleu_score, alternative="two.sided", paired=F)
  print(p)
  
  print("Cliff's delta for setters")
  method_types = unique(results.changed$method_type)
  for (type in method_types){
    mts.subset  <- results.changed[results.changed$method_type == type,]
    es <- cliff.delta(mts.subset$bleu_score, results.base$bleu_score)
    print(paste("For", type, "methods", sep=" "))
    print(es)
  }
  
  print("Results of the Permutation test")
  r <- aovperm(bleu_score ~ method_type, data = results.changed, np=500) # reduce the number if iterations (np) if you reach memory limit
  print(r)
  
  ## RQ2
  print("## RQ2") 
  print("Results of the Friedman test")
  print("Nemenyi post-hoc procedure (see plots)")
  x <- data.frame(results[results$transformations==1,]$bleu_score, 
                  results[results$transformations==5,]$bleu_score,
                  results[results$transformations==10,]$bleu_score
  )
  x <- as.matrix(x)
  colnames(x) <- c("1st Order", "5th Order", "10th Order")
  a <- nemenyi(x, conf.level=0.95, plottype="vmcb", main = "Nemenyi Test Results for #Transfarmations")
  
  print("## RQ2") 
  print("Results of the Friedman test")
  for (n_transformation in c(1,5,10)){
    results2 <- results[results$transformations == n_transformation, ]
    p <- friedman.test(data=results2, y=results2$bleu_score, groups = as.factor(results2$MT), blocks = as.factor(results2$entry))
    print(p)
    
    print("Nemenyi post-hoc procedure (see plots)")
    x <- data.frame(results2[results2$MT=="MT-IF",]$bleu_score, 
                    results2[results2$MT=="MT-L",]$bleu_score,
                    results2[results2$MT=="MT-NE",]$bleu_score,
                    results2[results2$MT=="MT-REP + MT-UVP",]$bleu_score,
                    results2[results2$MT=="MT-RER + MT-UVR",]$bleu_score
    )
    x <- as.matrix(x)
    colnames(x) <- c("MT-IF","MT-L","MT-NE","MT-REP + MT-UVP","MT-RER + MT-UVR")
    
    if (language == "java")
        lang = "Java"
    else
        lang = "Python"
    
    a <- nemenyi(x, conf.level=0.95, plottype="vmcb", main=paste(lang, "with", n_transformation, "transformations", sep = " "))
  }
  
  # Multi-factor analysis (RQ3)
  results2 <- results[results$transformations!="0" & results$different_to_ref=="True",]
  r <- aovperm(bleu_score ~ method_type*MT*transformations, data = results2, np=100) # reduce the number if iterations (np) if you reach memory limit
  print(r)
}


non_reference_changed_results <- full.results[full.results$transformations!="0" & full.results$different_to_ref=="True",]
aov_for_non_reference_changed_results  <- aovperm(bleu_score ~ method_type*MT*transformations*prefix*, data = non_reference_changed_results, np=100)
print(aov_for_non_reference_changed_results)
