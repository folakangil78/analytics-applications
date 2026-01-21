grocery_list = []

print('QUESTION 1 - List from Imported File')
print()

with open("GroceryList.csv", mode='r', encoding = "utf-8-sig") as f: # imports txt converted file from working direc of script
    # iterates for loop with iterable 'f' to insert line from file into list
    for line in f:
        grocery_list.append(line.strip()) # redacts white space after line quantity and appends actual quantity to grocery list (end of list)
print(grocery_list)

print()
print('QUESTION 2 - Redact Duplicates')
print()

duplicates= [] # initialize duplicates list

for i in range(len(grocery_list)): # traverses number of iterations per length of grocery list
    food = grocery_list[i]
    if grocery_list.index(food) != i and food not in duplicates:  # traverse grocery list for duplicate
        duplicates.append(food) # insert food element in duplicates if replica found at different index

if duplicates: # only prints if there any actual items in duplicates array
    print("Duplicate in grocery list:", duplicates)
    print()
else:
    print("No duplicates in grocery list.")
    print()

grocery_list_no_duplicates = [] # create new list without apples duplicates
for item in grocery_list:
    if item not in grocery_list_no_duplicates:  # inserts unique items in no_dupes list
        grocery_list_no_duplicates.append(item)
print("Updated grocery list without duplicates:\n\n", grocery_list_no_duplicates) # uses escape sequence \n to format spacing in console output

print()
print('QUESTION 3 - Add Meats')
print()

# appends chicken thighs and salmon to end of list without duplicates
grocery_list_no_duplicates.append('Chicken Thighs')
grocery_list_no_duplicates.append('Salmon')

print("Updated list with Chicken Thighs and Salmon:\n\n", grocery_list_no_duplicates) # uses escape sequence \n to format spacing in console output

# consider using .remove() from original grocery list based on elements in duplicates array instead of creating third array for sake of memory/efficiency