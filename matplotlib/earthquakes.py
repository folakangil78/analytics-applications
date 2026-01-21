import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

path='\\Users\\akula\\Desktop\\Comp 231\\Comp 231 Fall 2024\\'
data=pd.read_csv(path+'haiti_earthquake.csv')

data=data[(data.LATITUDE>18) & (data.LATITUDE<20) &
          (data.LONGITUDE>-75) & (data.LONGITUDE<-70) &
          data.CATEGORY.notnull()]

# Data wrangling -- resolves multiple categories in category field
# Make sure code is reported in English

def to_cat_list(catstr):
    stripped=(x.strip() for x in catstr.split(','))
    return [x for x in stripped if x]

def get_all_categories(cat_series):
    cat_sets=(set(to_cat_list(x)) for x in cat_series)
    return sorted(set.union(*cat_sets))

def get_english(cat):
    code,names=cat.split('.')
    if '|' in names:
        names=names.split(' | ')[1]
    return code, names.strip()

all_cats=get_all_categories(data.CATEGORY)

english_mapping=dict(get_english(x) for x in all_cats)

# We're going to use these codes for analysis.
# So we need to clean this up.

def get_code(seq):
    return [x.split('.')[0] for x in seq if x]

all_codes=get_code(all_cats)
code_index=pd.Index(np.unique(all_codes))
dummy_frame=pd.DataFrame(np.zeros((len(data),len(code_index))),
                      index=data.index, columns=code_index)

for row, cat in zip(data.index,data.CATEGORY):
    codes=get_code(to_cat_list(cat))
    for i in codes:
        dummy_frame.loc[row,i]=1

s = pd.Series(np.random.randn(10).cumsum(),index=np.arange(0,100,10))
s.plot()

df = pd.DataFrame(np.random.randn(10,4).cumsum(0),
                  columns=['Category'],
                  index=np.arange(columns.split(','))))
df.plot()

fig,axes = plt.subplots(2,1)
data = pd.Series(np.random.rand(16), index=list('abcdefghijlkmnop'))
data.plot(kind='bar',ax=axes[0],color='k',alpha=0.7)
data.plot(kind='barh',ax=axes[1],color='k',alpha=0.7)

df = pd.DataFrame(np.random.rand(6,4))
