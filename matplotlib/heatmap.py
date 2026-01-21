import numpy as np
import matplotlib.pyplot as plt
import pandas as pd

data = np.random.randint(0,100, size=(10,10))

plt.figure(figsize=(10,8))
plt.imshow(data, cmap='coolwarm', interpolation='nearest')

plt.colorbar()

for i in range(data.shape[0]):
    for j in range(data.shape[1]):
        plt.text(j, i, data[i, j], ha='center', va='center', color='black')

n_points = 100000
n_bins = 20

x = np.random.randn(n_points)
y = 0.4 * x + np.random.randn(100000) + 5

fig, axs = plt.subplots(1,2,sharey=True, tight_layout=True)

axs[0].hist(x, bins=n_bins)
axs[1].hist(y, bins=n_bins)

plt.title("Similarity Heatmap")

plt.show()

from datetime import datetime

s = pd.Series(np.random.randn(10).cumsum(),index=np.arange(0,100,10))
s.plot()

df = pd.DataFrame(np.random.randn(10,4).cumsum(0),
                  columns=['A', 'B', 'C', 'D'],
                  index=np.arange(0,100,10))
df.plot()

fig,axes = plt.subplots(2,1)
data = pd.Series(np.random.rand(16), index=list('abcdefghijlkmnop'))
data.plot(kind='bar',ax=axes[0],color='k',alpha=0.7)
data.plot(kind='barh',ax=axes[1],color='k',alpha=0.7)

df = pd.DataFrame(np.random.rand(6,4))

# Look at Annotating a Subplot slide in pwerpoint for matplotlib for stock vis