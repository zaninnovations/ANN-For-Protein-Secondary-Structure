# ANN-for-protien-secondary-structer
-----------------------------------------

**Approximately replecated methodology from ...**
  
 **[Using knowledge-based neural networks to improve algorithms:  
 Refining the Chou-Fasman algorithm for protein folding](http://link.springer.com/article/10.1007/BF00993077)**

------------------------------------------

**Data Set:** 

Test set for study of secondary structure of globular proteins by
**Ning Qing** and **Terry Sejnowski.** 

There were 128 protiens in the UC-Irvine archive. Train and Test were combined into one file (with train in front).
Data was split into three test sets for our version of this network: Train, Tune and Test.  
  
  
Out of 128 proteins, they were broken up as follows:  
* tune: index % 5 = 0  
* train: index % 6 = 0  
* test: the rest of the proteins.


---------------------------------------------

**Network:**
  
  
This network is implemented with:
* Hinton's Droput 
* Momentum Term for backpropagation
* Early Stopping


Network preforms best with the configuration found in the main method.  


---------------------------------------------
