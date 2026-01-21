import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import numpy as np

# plt.plot([1, 2, 3, 4], [1, 4, 9, 16])

# plt.axis([0, 6, 0, 20])

# plt.xlabel("X-axis label")
# plt.ylabel("Y-axis label")

# plt.show()

file = 'Foreign_Exchange_Rates.csv'
df = pd.read_csv(file, usecols=[1,7], names=['DATE', 'CAD_USD'],skiprows=1,
                 index_col=0,parse_dates=[0])

df['CAD_USD'] = pd.to_numeric(df.CAD_USD, errors='coerce')
df.dropna(inplace=True)

df_m = df.copy()
df_m['month'] = [i.month for i in df_m.index]
df_m['year'] = [i.year for i in df_m.index]

df_m = df_m.groupby(['month', 'year']).mean()

fig, ax = plt.subplots()
sns.heatmap(df_m, cmap="Blues", vmin=0.9, vmax=1.65, linewidth=0.3,
            cbar_kws={"shrink":.8})

ax.xaxis.tick_top()
xticks_labels = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
plt.xticks(np.arange(12) + .5, labels=xticks_labels)
plt.xlabel('')
plt.ylabel('')
title = 'monthly Average exchange rate\nValue of one USD in CAD\n'.upper()
plt.title(title, loc='left')
plt.show()


