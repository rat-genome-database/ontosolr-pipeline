select concat('RGD_GENE:',a.RGD_ID) as term_acc, 'RGD_GENE' as onto_id, a.gene_symbol as term, concat(to_clob(concat (a.FULL_NAME, '; ')) ,a.GENE_DESC) as def, '(null)' as anc,
b.ALIAS_VALUE as synonyms,
lower(a.common_name) as species
from
(
select g.RGD_ID, g.gene_symbol, g.FULL_NAME, g.GENE_DESC, st.common_name
from
genes g ,
rgd_ids ri ,
species_types st
where
g.rgd_id = ri.rgd_id
and ri.species_type_key=st.species_type_key
//ORDER BY g.RGD_ID
) a
left join
(
select
genes.rgd_id,
aliases.ALIAS_KEY,
aliases.alias_value
from
RGD_IDS ,
genes,ALIASES
where aliases.RGD_ID = rgd_ids.RGD_ID
and genes.RGD_ID = aliases.RGD_ID
and ALIAS_TYPE_NAME_LC in ('old_gene_name', 'old_gene_symbol')
and RGD_IDS.object_status='ACTIVE'
//order by aliases.RGD_ID
) b
on a.RGD_ID = b.rgd_id
//ORDER BY a.RGD_ID;
