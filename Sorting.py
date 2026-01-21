import csv

def descend_insertion_sort(arr): # descending order, largest value first
    max = 0
    descending_arr = []

    i = 0
    max_index = 0
    decreasing_len_arr = len(arr)
    while i < decreasing_len_arr: #while loop to iterate for length of array that goes down as popping max value
        for j in range(1, len(arr)): #starts iterating at second value to compare to one before it
            if arr[j] > arr[j-1]: #compares to prev value
                max = arr[j]
                max_index = j #sets max to value if condition is true and caches max
        descending_arr.append(max) #appends final max val after for loop is one
        arr.pop(max_index) #deletes max from old array, idk why pop index is out of range..
        i+=1
    return descending_arr

def descend_insertion_sort_redone(arr): # descending order, largest value first
    # Traverse through 1 to len(arr)
    for i in range(1, len(arr)):
        key = arr[i]  # The element to be inserted in the sorted portion
        j = i - 1
        # Move elements of arr[0..i-1], that are smaller than key, one position ahead
        # of their current position to make space for key
        while j >= 0 and arr[j] < key:
            arr[j + 1] = arr[j]
            j -= 1
        arr[j + 1] = key  # Place key at correct position
    return arr

def ascend_insertion_sort(arr): # ascending order, smallest value first
    min = 0
    ascending_arr = []

    i = 0
    min_index = 0
    decreasing_len_arr = len(arr)
    while i < decreasing_len_arr:
        for j in range(1, len(arr)):
            if arr[j] < arr[j-1]: #checks previous element for being smaller and caches if true
                min = arr[j]
                min_index = j
        ascending_arr.append(min) 
        arr.pop(min_index) # idk why pop index is out of range..
        i+=1
    return ascending_arr


def main():
    with open('PS4_test_dataset_Q1and2.txt', 'r') as input_file:
        numbers = input_file.read()
        separate_nums = numbers.split(',')
        numbers_arr = [int(num) for num in separate_nums] # reads file in with casting iterator var to int

    descending_arr = descend_insertion_sort_redone(numbers_arr)
    # ascending_arr = ascend_insertion_sort(numbers_arr)

    for element in descending_arr:
         print(element, end=', ')

    # for element in ascending_arr:
    #     print(element, end=', ')
main()