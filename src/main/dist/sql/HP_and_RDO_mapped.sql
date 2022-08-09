select t2.term_acc,t2.term,t1.term_acc,t1.term
from ont_ext_relationship r,ont_terms t1,ont_terms t2
where term_acc1=t1.term_acc and term_acc2=t2.term_acc
