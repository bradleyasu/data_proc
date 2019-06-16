import pandas as pd
from fuzzywuzzy import fuzz



def match_data(proc_item, fin_data, min_score=0):
    max_score = -1
    max_name = ""
    for item in fin_data.itertuples():
        if(item.location_city == proc_item.city):
            score = fuzz.ratio(name, n)
            if (score > min_score) & (score > max_score):
                max_name = n
                max_score = score
    return (max_name, max_score)




# Import Finance Data
df_fin_address = pd.read_csv("data/finance/factset__ent_entity_address.csv")
df_fin_coverage = pd.read_csv("data/finance/factset__ent_entity_coverage.csv")
df_fin_structure = pd.read_csv("data/finance/factset__ent_entity_structure.csv")
    
# Import Procurement Data

df_pro_geo = pd.read_csv("data/procurement/mdl__dim_geo.csv")
df_pro_vendor = pd.read_csv("data/procurement/mdl__dim_vendor.csv")


# Combine Finance Data
finance_data = df_fin_structure.merge(df_fin_address, left_on='factset_entity_id', right_on='factset_entity_id', how='outer').merge(df_fin_coverage, left_on='factset_entity_id', right_on='factset_entity_id', how='outer')

# Combine Procurement Data
procurement_data = df_pro_geo.merge(df_pro_vendor, left_on='geo_id', right_on='geo_id', how='outer')

print "Matching data..."
for row in procurement_data.itertuples():
    print row.name
    # match = match_names(row, finance_data)
    # print row.name + " --> " + str(match)

