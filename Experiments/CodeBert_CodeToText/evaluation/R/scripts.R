if (!require(rstatix)){ install.packages("rstatix") }
if (!require(effsize)){ install.packages("effsize") }
if (!require(PMCMR)){ install.packages("PMCMR") }
if (!require(tsutils)){ install.packages("tsutils") }
if (!require(permuco)){ install.packages("permuco") }

## read csv file
full.results <- read.csv("bleus.csv")

# Adding the "gold_length" changed some results drastically
# So the below scripts check the values, but they look ok 
# length(unique(full.results$gold_length_in_characters)) # -> 353
# length(unique(full.results$gold_length_in_words)) # -> 87



for (language in unique(full.results$prefix)){
  print(paste("## RESULTS FOR", language))
  
  results <- full.results[full.results$prefix==language, ]
  
  print(length(unique(results$method_type)))
  # reference results
  results.base <- results[results$config=="reference", ]
  
  ## RQ1
  print("## RQ1") 
  # 1. Results for first-order transformations
  results.changed <- results[results$transformations=="1", ]
  
  # Statistical significance for RQ1 considering all data values
  p_wilcoxon_bleu_all <- wilcox.test(results.base$bleu_score, results.changed$bleu_score, alternative="two.sided", paired=F)
  print("Statistical significance for BLEU-Score considering the entire test set") 
  print(p_wilcoxon_bleu_all)
  
  print("Statistical significance for BLEU-Score considering the code snippets with changes") 
  results.changed <- results[results$transformations=="1" & results$different_to_ref=="True", ]
  # This wilcoxon test changes whether the distribution of bleu-scores for non-changed and for changed elements come from different distributions
  p_wilcoxon_bleu_only_changed <- wilcox.test(results.base$bleu_score, results.changed$bleu_score, alternative="two.sided", paired=F)
  print(p_wilcoxon_bleu_only_changed)
  
  print("Cliff's delta for Specific Method-Types")
  method_types = unique(results.changed$method_type)
  for (type in method_types){
    # This Test checks for the effect size (how important) a certain method type is for the bleu score
    # It does so by comparing the differences in distributions
    mts.subset  <- results.changed[results.changed$method_type == type,]
    effect_size_per_method <- cliff.delta(mts.subset$bleu_score, results.base$bleu_score)
    print(paste("For", type, "methods", sep=" "))
    print(effect_size_per_method)
  }
  
  results.transformations_applied <- results[results$transformations=="1", ]
  print("Results of the Permutation test")
  print("Method Types", length(unique(results.transformations_applied$method_type)))
  r_per_method <- aovperm(bleu_score ~ method_type, data = results.transformations_applied, np=500) # reduce the number if iterations (np) if you reach memory limit
  print(r_per_method)
  
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
  a <- nemenyi(x, conf.level=0.95, plottype="vmcb", main = paste("Nemenyi Test Results for #Transformations in",language))
  
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
  # I think these are redundant, given the big Anova below
  # 
  # changed_and_different_results_per_language <- results[results$transformations!="0" & results$different_to_ref=="True",]
  # aov_results_per_lang <- aovperm(bleu_score ~ method_type*MT*transformations, data = changed_and_different_results_per_language, np=100) # reduce the number if iterations (np) if you reach memory limit
  # print(aov_results_per_lang)
}

# RQ2 & 3 --- Nemenyi for Un-shared Transformations and MTs
t_bleus <- data.frame(full.results[full.results$transformations==1,]$bleu_score, 
                      full.results[full.results$transformations==5,]$bleu_score,
                      full.results[full.results$transformations==10,]$bleu_score
)
t_bleus <- as.matrix(t_bleus)
colnames(t_bleus) <- c("1st Order", "5th Order", "10th Order")
t_bleus_nemenyi <- nemenyi(t_bleus, conf.level=0.95, plottype="vmcb", main = "Nemenyi Test Results for #Transformations")

non_reference_results <- full.results$config!="reference"

mt_bleus <- data.frame(non_reference_results[non_reference_results$MT=="MT-IF",]$bleu_score, 
                       non_reference_results[non_reference_results$MT=="MT-L",]$bleu_score,
                       non_reference_results[non_reference_results$MT=="MT-NE",]$bleu_score,
                       non_reference_results[non_reference_results$MT=="MT-REP + MT-UVP",]$bleu_score,
                       non_reference_results[non_reference_results$MT=="MT-RER + MT-UVR",]$bleu_score
)
mt_bleus <- as.matrix(mt_bleus)

colnames(mt_bleus) <- c("MT-IF","MT-L","MT-NE","MT-REP + MT-UVP","MT-RER + MT-UVR")

mt_bleus_nemenyi <- nemenyi(mt_bleus, conf.level=0.95, plottype="vmcb", main = "Nemenyi Test Results for MTs")


# RQ3 --- Big ANOVA-Check for Correlations
# Approx Times: np=100 -> 2 min, np=500 -> 11 min
print("Results for the 'Big' Anova Perm")
non_reference_changed_results <- full.results[ful5l.results$transformations!="0" & full.results$different_to_ref=="True",]
aov_for_non_reference_changed_results  <- aovperm(bleu_score ~ method_type*MT*transformations*prefix*gold_length_in_words, data = non_reference_changed_results, np=1000)
print(aov_for_non_reference_changed_results)

