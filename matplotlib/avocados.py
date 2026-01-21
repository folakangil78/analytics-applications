import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# reading in avo_data and cleaning
avo_data = pd.read_csv("avocado.csv")

# parsing date col to convert to datetime obj format, easier to access year quantity then
avo_data['Date'] = pd.to_datetime(avo_data['Date'], format='%m/%d/%y')

# creates df with years greater than or equal to 2015 but less than or equal to 2017 using datetime obj.year attribute
filtered_avo_data = avo_data[(avo_data['Date'].dt.year >= 2015) & (avo_data['Date'].dt.year <= 2017)]

# excluding rows with negative sale numbers - not useful data
filtered_avo_data = filtered_avo_data[filtered_avo_data['Number_Avocados_Sold'] >= 0]

# PLOT 1:

# filtering cleanded data specifically based on ordering of dates and organic/conventional
date_type_avodata = filtered_avo_data.groupby(['Date', 'type'])['Number_Avocados_Sold'].sum().reset_index()
# summing num sold -- based on type and date it was sold on -- so that all the conventional-types sold on a specific day 
# and all the organic sold on a specific day are aggregated as indiv, separate pts

# separate dfs based on conventional and organic for separate line plots to show change/growth
conventional_avo_data = date_type_avodata[date_type_avodata['type'] == 'conventional']
organic_avo_data = date_type_avodata[date_type_avodata['type'] == 'organic']

plt.figure(figsize=(12, 6))
plt.plot(conventional_avo_data['Date'], conventional_avo_data['Number_Avocados_Sold'], label='Conventional', color='magenta', alpha=0.7)
plt.plot(organic_avo_data['Date'], organic_avo_data['Number_Avocados_Sold'], label='Organic', color='orange', alpha=0.7)
# setting ordered date as x-arg, given type-specification as y-arg, and specifies legend titles

plt.title('Sales Trends of Conventional and Organic Avocados (2015-2017) ~ Plot 1', fontsize=13)
plt.xlabel('Date', fontsize=12)
plt.ylabel('Number of Avocados Sold (Hundreds of Thousands)', fontsize=11)
plt.legend(title='Type', fontsize=11)
plt.grid(alpha=0.5)
plt.tight_layout()
plt.show()

# PLOT 2:
# NOTE TO GRADER: Dr. Kula clarified that i wouldnt have to delineate between broader regions and specific cities in the region column when 
# visualizing this (as long as i specified this in the plot) b/c there are no differentiating formatting choices in the actual data to help parse with pandas between cities and regions

# creating new col within cleaned df specifically containing year attrib from datetime obj of date col
filtered_avo_data['Year'] = filtered_avo_data['Date'].dt.year

# grouping by place and year col, summing avocado sales again
region_year_sales = filtered_avo_data.groupby(['region', 'Year'])['Number_Avocados_Sold'].sum().unstack(fill_value=0)

# sorting within stacked bar chartSort the regions by the total avocados sold across all years
region_year_sales['Total'] = region_year_sales.sum(axis=1)
region_year_sales = region_year_sales.sort_values('Total', ascending=False)

# stacked bar plot based on year for all top regions
plt.figure(figsize=(14, 7))

# chose to plot top 10, within stacked bars (stacked arg in plot() fxn)
region_year_sales.head(10).drop(columns='Total').plot(kind='bar', stacked=True, figsize=(14, 7))
plt.title('Top Regions/Cities by Avocado Sales (2015-2017) ~ Plot 2', fontsize=13.5)
plt.xlabel('Region/City', fontsize=12)
plt.ylabel('Number of Avocados Sold (Tens of Billions ~ Scientific Notation)', fontsize=12)
plt.xticks(rotation=45, ha='right')
plt.legend(title='Year', title_fontsize=12, fontsize=10)
plt.tight_layout()
plt.show()

# PLOT 3:

# specify cleaned data based on given cities
selected_cities = ["Chicago", "MiamiFtLauderdale", "LosAngeles", "Boston"]
city_avo_data = filtered_avo_data[filtered_avo_data['region'].isin(selected_cities)]

# create new month col with city df using month attribute of datetime objects
city_avo_data['Month'] = city_avo_data['Date'].dt.to_period('M') # 'M' groups by month

# grouping by given city and finding average price across all days in a given month, inserting new avgprice col
monthly_avg_price = city_avo_data.groupby(['region', 'Month'])['AveragePrice'].mean().reset_index()

# uses pivot function by taking inflection values from monthly_avg df and creates df of those specific values to insert as axis-tickmarks
price_pivot = monthly_avg_price.pivot(index='Month', columns='region', values='AveragePrice')

plt.figure(figsize=(14, 7))
for city in selected_cities:
    plt.plot(price_pivot.index.astype(str), price_pivot[city], label=city)
    # for loop to iterate thru specific cities and plot avg prices
plt.title('Monthly Average Avocado Prices (2015-2017) ~ Plot 3', fontsize=16)
plt.xlabel('Month', fontsize=14)
plt.ylabel('Average Price (USD)', fontsize=14)
plt.xticks(rotation=45, fontsize=10)
plt.yticks(fontsize=10)
plt.legend(title='City', title_fontsize=12, fontsize=10)
plt.grid(axis='y', linestyle='--', alpha=0.7)
plt.tight_layout()
plt.show()

# PLOT 4:
# NOTE TO GRADER: Dr. Kula clarified that i wouldnt have to specify between broader regions and specific cities in the region column when 
# visualizing this b/c there are no differentiating formatting choices in the actual data to help parse with pandas between cities and regions

# finding top 5 regions and/or cities with highest avocado sales with head(5) function and indexing
top_regions = (
    filtered_avo_data.groupby('region')['Number_Avocados_Sold']
    .sum()
    .sort_values(ascending=False)
    .head(5)
    .index
)
# pretty much translated from plot 2 code showing top regions/cities for sales broadly

# indexes through filtered/cleaned data for given top regions calculated above
top_regions_avo_data = filtered_avo_data[filtered_avo_data['region'].isin(top_regions)]

# organizes by type of organic or conventional and sums total sales
sales_by_type = (
    top_regions_avo_data.groupby(['region', 'type'])['Number_Avocados_Sold']
    .sum()
    .unstack(fill_value=0) # pivoting values again to acccess values for graphing/axes
)

fig, ax = plt.subplots(figsize=(10, 6))
sales_by_type.plot(kind='bar', stacked=True, ax=ax, color=['purple', 'green'])
ax.set_title('Proportion of Conventional vs. Organic Avocado Sales in Top 5 Regions ~ Plot 4', fontsize=16)
ax.set_xlabel('Region', fontsize=14)
ax.set_ylabel('Number of Avocados Sold (Tens of Billions)', fontsize=14)
ax.legend(title='Type', title_fontsize=12, labels=['Conventional', 'Organic'])
plt.xticks(rotation=45, fontsize=10) # rotation of labels impt for readability
plt.yticks(fontsize=10)
ax.grid(axis='y', linestyle='--', alpha=0.7)
plt.tight_layout()
plt.show()
