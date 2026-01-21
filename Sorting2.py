import csv

def quick_sort(values_arr): #ordering in ascending order, smallest value first
    if len(values_arr) <= 1: #ensuring return if length is < 1, meaning already sorted
        return values_arr

    pivot = values_arr[len(values_arr) // 2] #locates middle value
    middle = [x for x in values_arr if x == pivot] #organizes all elements that are same as pivot val
    right = [x for x in values_arr if x > pivot] #organizes values greater than pivot value
    left = [x for x in values_arr if x < pivot] #recursively sorts sections of values less than/greater than/equal to pivot

    # Recursively sort left and right partitions, then combine
    return quick_sort(left) + middle + quick_sort(right)

def quick_sort_comparison(values_arr): #ordering in ascending order, smallest value first
    comparisons = 0
    if len(values_arr) <= 1: #ensuring return if length is < 1, meaning already sorted
        return comparisons,values_arr

    pivot = values_arr[len(values_arr) // 2] #locates middle value
    middle = [x for x in values_arr if x == pivot] #organizes all elements that are same as pivot val
    right = [x for x in values_arr if x > pivot] #organizes values greater than pivot value
    left = [x for x in values_arr if x < pivot] #recursively sorts sections of values less than/greater than/equal to pivot

    comparisons += len(values_arr) - 1  # comparing all elements in the array to the pivot

    left_comparisons, sorted_left = quick_sort_comparison(left)
    right_comparisons, sorted_right = quick_sort_comparison(right)

    comparisons += left_comparisons + right_comparisons

    # Recursively sort left and right partitions, then combine
    return comparisons, quick_sort(left) + middle + quick_sort(right)

def bubble_sort(A): #Dr. Kula's code
    comparisons = 0
    for i in range(len(A)):
        swap=False
        for j in range(len(A)-i-1):
            comparisons+=1
            if A[j]>A[j+1]:
                A[j],A[j+1]=A[j+1],A[j]
                swap=True
        if swap==False:
            return comparisons, A
    return comparisons,A

def selection_sort(A): #Dr. Kula's code
    comparisons = 0
    for i in range(len(A)):
        min_index = i
        for j in range(i+1,len(A)):
            comparisons+=1
            if A[min_index]>A[j]:
                min_index=j
        A[i],A[min_index]=A[min_index],A[i]
    return comparisons,A


# FUNCTION TO PRINT OUTPUT FOR #1 ON EXERCISE
def output_to_csv(arr, filename):  #writing output to csv
    sorted_array = quick_sort(arr)  #calls quicksort function already to make main fxn call more readable

    with open(filename, mode='w', newline='') as file:
        writer = csv.writer(file)
        for num in sorted_array:
            writer.writerow([num])  #inserts sorted value on separate row

def main():
    with open('PS4_test_dataset_Q1and2.txt', 'r') as input_file:
        numbers = input_file.read()
        separate_nums = numbers.split(',')  # splits values based on commas
        numbers_arr = [int(num) for num in separate_nums]  #converts string numbers to integers,reads file in with casting iterator var to int

    output_filename = 'quicksort_sorted_vals.csv'
    output_to_csv(numbers_arr, output_filename)  # output csv fxn already calls quick_sort fxn

    print(f'Sorted list outputted to file in directory with name {output_filename}\n') #Question 1

    #2 here
    comparisons,a=quick_sort_comparison(numbers_arr)
    print(str(comparisons)+' HAVE BEEN MADE WITH QUICK SORT.\n')
    print(a)

    comparisons2,a2=bubble_sort(numbers_arr)
    print(str(comparisons2)+'\n HAVE BEEN MADE WITH BUBBLE SORT.\n')
    print(a2)

    comparisons3,a3=selection_sort(numbers_arr)
    print(str(comparisons3)+'\n HAVE BEEN MADE WITH SELECTION SORT.\n')
    print(a3)

main()