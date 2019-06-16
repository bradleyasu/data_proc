import pandas as pd
import difflib
from fuzzywuzzy import fuzz




def match_data(fin_item, proc_data, min_score=0):
    max_score = -1
    max_name = ""
    for item in proc_data.itertuples():
        if(fin_item.iso_country_y == item.country_y):
            score = fuzz.ratio(fin_item.entity_name, item.name)
            if (score > min_score) & (score > max_score):
                max_name = item.name
                max_score = score
        return (max_name, max_score)




print "Importing Data..."

# Import Finance Data
df_fin_address = pd.read_csv("data/finance/factset__ent_entity_address.csv")
df_fin_coverage = pd.read_csv("data/finance/factset__ent_entity_coverage.csv")
df_fin_structure = pd.read_csv("data/finance/factset__ent_entity_structure.csv")
    
# Import Procurement Data

df_pro_geo = pd.read_csv("data/procurement/mdl__dim_geo.csv")
df_pro_vendor = pd.read_csv("data/procurement/mdl__dim_vendor.csv")


print "Joining Finance Data..."
# Combine Finance Data
finance_data = df_fin_structure.merge(df_fin_address, left_on='factset_entity_id', right_on='factset_entity_id', how='outer').merge(df_fin_coverage, left_on='factset_entity_id', right_on='factset_entity_id', how='outer')

print "Joining Procurement Data..."
# Combine Procurement Data
procurement_data = df_pro_geo.merge(df_pro_vendor, left_on='geo_id', right_on='geo_id', how='outer')


print "Clustering data..."
# fin_clusters = {}
# proc_clusters = {} 
# for row in finance_data.itertuples():
#     # print row
#     if not row.iso_country_y in fin_clusters:
#         fin_clusters.update({row.iso_country_y: []})
#     fin_clusters[row.iso_country_y].append(row)

# for row in procurement_data.itertuples():
#     # print row
#     if not row.country_iso2 in proc_clusters:
#         proc_clusters.update({row.country_iso2: []})
#     proc_clusters[row.country_iso2].append(row)

# for iso in fin_clusters:
#     if not iso in proc_clusters:
#         print iso + " in fin but not proc"


print "Matching data..."


def get_donors(row):
    d = finance_data.apply(lambda x: fuzz.ratio(x['entity_name'], row['name']) * 2 if row['country_iso2'] == x['iso_country_y'] else 1, axis=1)
    d = d[d >= 90]
    if len(d) == 0:
        v = ['']*2
    else:
        v = finance_data.ix[d.idxmax(), ['entity_name','iso_country_y']].values
    return pd.Series(v, index=['entity_name', 'iso_country_y'])

merged = pd.concat((procurement_data, procurement_data.apply(get_donors, axis=1)), axis=1)

merged.to_csv("output.csv")



# for row in finance_data.itertuples():
#     # print row.name
#     match = match_data(row, procurement_data)
#     print row.entity_name + " --> " + str(match)

