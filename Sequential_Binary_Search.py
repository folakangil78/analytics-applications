# I provided a sample txt file. This function reads it.
def read_data_from_file(fileName): #takes in file to parse out data
    readFile = open(fileName, 'r').readlines() #open file and read each line as an index in a list
    n1 = int(readFile[0]) #first index is first array length
    n2 = int(readFile[1]) #second index is second array length
    sa1 = readFile[2].split(' ') #first list made separating indexes when hit ' ' character
    a1 = [int(x) for x in sa1] #since txt file, read as string and converted to int
    sa2 = readFile[3].split(' ')
    a2 = [int(x) for x in sa2]
    return n1, n2, a1, a2 #return the parameters needed from file


def find_indices(n,m,A,values):
    # n = size of A
    # m = size of values (ints I'm going to search for in A)
    
    output=''
    for i in range(m):
        found_flag=False
        # Note, can't just do python in/not in or index. To implement the algorithm, you need to loop through it.
        for j in range(n):
            if A[j]==values[i]:
                if found_flag==False:
                    output+=str(j+1)+' '
                    # can include a line here to exit the j loop (since it's found)
                found_flag=True
        if found_flag==False: # didn't find it
            output+='-1 '
    return output

n,m,A,values=read_data_from_file('PS3_search_dataset.txt')
print(find_indices(n,m,A,values))

# Only doing Question 2 - Binary Search