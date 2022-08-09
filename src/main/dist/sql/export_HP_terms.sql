select t.term_acc,t.ONT_ID,t.term as term, t.TERM_DEFINITION as def,
(
select   LISTAGG(unique concat(' ', concat(o1.term_acc, concat('; ', o1.term))),',')
     within group(order by o1.term_acc)

 from ont_dag, ont_terms o1
where o1.term_acc = ont_dag.parent_term_acc
start with child_term_acc=t.term_acc
connect by prior parent_term_acc=child_term_acc
) as anc,
synonyms
from ont_terms t
left join
(select term_acc as acc, synonym_name as synonyms
from ont_synonyms where ont_synonyms.SYNONYM_TYPE in ('narrow_synonym', 'related_synonym','broad_synonym', 'synonym', 'exact_synonym')) s
on t.TERM_ACC = s.acc
where t.IS_OBSOLETE = 0
and t.ONT_ID='HP'
order by t.term_acc
