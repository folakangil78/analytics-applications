import pandas as pd
import numpy as np
import seaborn as sns

# Create a simple DataFrame using pandas
data = {
    'Name': ['Alice', 'Bob', 'Charlie', 'David'],
    'Age': [25, 30, 35, 40],
    'Salary': [50000, 60000, 70000, 80000]
}
df = pd.DataFrame(data)

# Print the DataFrame
print("Pandas DataFrame:")
print(df)

# Perform a basic operation on the DataFrame (e.g., add a new column)
df['Bonus'] = df['Salary'] * 0.1
print("\nDataFrame after adding 'Bonus' column:")
print(df)

# Create a numpy array and perform an operation
np_array = np.array([1, 2, 3, 4, 5])
print("\nNumpy Array:")
print(np_array)

# Perform some basic numpy operations
np_sum = np_array.sum()
np_mean = np_array.mean()
print(f"\nSum of numpy array: {np_sum}")
print(f"Mean of numpy array: {np_mean}")

# Combine pandas and numpy by performing an operation using both
df['Adjusted Salary'] = df['Salary'] + np_array[:len(df)] * 5000
print("\nDataFrame after adding 'Adjusted Salary' using numpy array:")
print(df)
print('HELLOW THIS WORKED')

# df['submission_date'] = pd.to_datetime(df['submission_date'])
# df['Month'] = 

titanic = sns.load_dataset('titanic')
print(titanic.head())