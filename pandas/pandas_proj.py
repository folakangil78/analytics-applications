import pandas as pd

# Question 1______

# Reads in given dataset
df = pd.read_csv('1980sClassics.csv')

# Queries through df and sorts by Year and Valence - mean function finds average across diff years
avg_valence_per_year = df.groupby('Year')['Valence'].mean().reset_index()

# Labels columns
avg_valence_per_year.columns = ['Year', 'Average valence']

# Outpus grouped-by df to csv file
avg_valence_per_year.to_csv('avg_valence_per_year.csv', index=False)

# Question 2______

# Function to split quantity in duration column by mins and secs, multiplying given mins by 60 for secs conversion and adding to given secs
def duration_to_secs(duration):
    mins, secs = map(int, duration.split(':'))
    return mins * 60 + secs

# Executes duration time-conversion fxn to actual column and relabels columns
df['Duration (secs)'] = df['Duration'].apply(duration_to_secs)

# Basically sorting all tracks by given duration
bins = [0, 60, 180, 300, float('inf')] # Creates list of bins to append each track under
labels = ['<1 min', '1-3 min', '3-5 min', '>5 min'] # Equivalent list to bins for csv output, labeling for duration categories
df['Duration Category'] = pd.cut(df['Duration (secs)'], bins=bins, labels=labels)
# Cut function used to separate tracks based on duration

# Dataframe to append top 5 tracks with highest danceability in each duration
top_5_danceability = pd.DataFrame()

for category in labels:
    # Filters duration column of df based on highest danceability values, head function takes the top five and appends
    category_df = df[df['Duration Category'] == category].sort_values(by='Danceability', ascending=False).head(5)
    
    # Combines top 5 songs from each duration into single df
    top_5_danceability = pd.concat([top_5_danceability, category_df[['Track', 'Artist', 'Duration (secs)', 'Danceability']]])

# Output to csv
top_5_danceability.to_csv('top_5_danceability.csv', index=False)

# Question 3______

# Tallying total num of songs each artist has released in new df
artist_song_counts = df['Artist'].value_counts()

# Creates new df based on tallies indexed at greater than 5
eligible_artists = artist_song_counts[artist_song_counts >= 5].index

# Creates additional df from given data based on artists with 5 or more to retain rest of information for these track entries
df_filtered = df[df['Artist'].isin(eligible_artists)]

# Tallying counts for mode=1/0 for each filtered artist
mode_counts = (
    df_filtered.groupby('Artist')
    .agg( # agg function "aggregates" calculations for each track entry
        total_songs=('Track', 'count'), # Counts total number of tracks per artist
        mode_1_count=('Mode', lambda x: (x == 1).sum()), # Counting number of tracks with mode=1
        mode_0_count=('Mode', lambda x: (x == 0).sum()) # Number of tracks with mode=0
        # Lambda functions increments variable (the sum fxn) for each time the condition is fulfilled: x==1 or x==0
    )
    .reset_index()
)

# Basic outputting to csv here
mode_counts.to_csv('mode_count.csv', index=False)

# Question 4______

# Find average popularity for each year and sort new df with average and associated year
yearly_avg_popularity = df.groupby('Year')['Popularity'].mean().reset_index()

# Designates separate dfs for odd v even years
yrs_even = yearly_avg_popularity[yearly_avg_popularity['Year'] % 2 == 0]
yrs_odd = yearly_avg_popularity[yearly_avg_popularity['Year'] % 2 != 0]

# Sort even and odd dfs in descending order of pop. value
yrs_even_sorted = yrs_even.sort_values(by='Popularity', ascending=False)
yrs_odd_sorted = yrs_odd.sort_values(by='Popularity', ascending=False)

# Total averages across both odd and even years, respectively
avg_even = yrs_even['Popularity'].mean()
avg_odd = yrs_odd['Popularity'].mean()

# Condition block to compare whether even or add has higher popularity
if avg_even > avg_odd:
    largest_pop = f"Even years had the highest popularity: {avg_even:.2f}"
else:
    largest_pop = f"Odd years had the highest popularity: {avg_odd:.2f}"

with open('even_odd.csv', 'w') as ouput_file: # 'w' attribute implies writing permissions to given file, with open block automatically closes csv file
    # Outputting even years even years section, still using same to_csv function but need to insert header text and even/odd comparison text at bottom
    ouput_file.write("Even year average popularity scores in descending order:\n")
    yrs_even_sorted.to_csv(ouput_file, index=False, header=True)
    ouput_file.write("\n\n")

    # Odd years header and csv df output
    ouput_file.write("Odd year average popularity scores in descending order:\n")
    yrs_odd_sorted.to_csv(ouput_file, index=False, header=True)
    ouput_file.write("\n\n")

    # Inserting concluding comparison between averages across odd and even years popularity
    ouput_file.write(largest_pop)

# Question 5______

# Lists for categories of bpm values and corresponding labels, need to cast inf to float to represent bpms more than 200
bins_tempo = [0, 70, 80, 100, 110, 156, 168, 200, float('inf')]
labels_tempo = ['Adagio', 'Andante', 'Moderato', 'Allegretto', 'Allegro', 'Vivace', 'Presto', 'Prestissimo']

# Designate each track into associated bin based on Tempo column
df['Tempo Group'] = pd.cut(df['Tempo'], bins=bins_tempo, labels=labels_tempo, right=False)

# Tallies number of tracks that fall into each tempo group, groupby artists
grouped = (
    df.groupby(['Tempo Group', 'Artist'], observed=True) # Impt to set observed param to True to denote that only groups (tracks) that are in df by tempo group and artist are actually queried
    # Observed param basicallly saying that unused tempo groups will be neglected
    .size()  # Count number of tracks
    .reset_index(name='Number of Tracks') # Renames Track count column to match output
)

# Applies head function for artists with most releases within given tempo category
top_artists_for_tempo_cat = (
    grouped.sort_values(['Tempo Group', 'Number of Tracks'], ascending=[True, False]) # Sorts by artists with top number of tracks
    .groupby('Tempo Group', observed=True) # Look above for observed param necessity
    .head(2) # Top 2 artists for each tempo group
)

# Switches columns to insert Artist first in sorted df for required output format
top_artists_for_tempo_cat = top_artists_for_tempo_cat[['Artist', 'Tempo Group', 'Number of Tracks']]

# Output..
top_artists_for_tempo_cat.to_csv('tempo_binned.csv', index=False)

