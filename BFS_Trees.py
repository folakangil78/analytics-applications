from collections import deque

class Node:
    def __init__(self,data):
        self.data = data
        self.right_child = None #initializes node banches in node object
        self.left_child = None #same here

def insert_BTnode(root, node): #function provided by Dr. Kula
    if root is None:
        root = node
    else:
        if root.data > node.data:
            if root.left_child is None: #possibly typo here that i edited to root.left_child because need to access possible left child
                # before its assoc branch
                root.left_child = node
            else:
                insert_BTnode(root.left_child, node)
        else:
            if root.right_child is None:
                root.right_child = node
            else:
                insert_BTnode(root.right_child, node)

# function to conduct breadth-first-search approach for bst
def BFS_my_BST(root):
    queue = deque([root])  #deck initialization with root, credit for initialization: GeeksforGeeks
    result = []  # To store the node values in BFS order

    while queue:
        current_node = queue.popleft() #uses popleft fxn for deck to delete node at beginning
        result.append(current_node.data) #inserts node at current slot into deck

        # Add left and right children to the queue if they exist
        if current_node.left_child:
            queue.append(current_node.left_child)
        if current_node.right_child:
            queue.append(current_node.right_child)
        #logic here only inserts additional left and right childs from insert_btnode if exists (thus conditional logic)
    return result

#Make my Binary Search Tree - provided by Dr. Kula
def make_my_BST(file_name):
    with open(file_name,'r') as f:
        nodes=f.readlines()
    list_of_values=[int(i.strip()) for i in nodes]
    
    my_root=Node(list_of_values[0])
    for i in list_of_values[1:]:
        insert_BTnode(my_root,Node(i))
    return my_root

file='bst_data.txt'
x=make_my_BST(file)
result = BFS_my_BST(x)
print(result)

# just some sample code
my_root = Node(3)
insert_BTnode(my_root,Node(7))
insert_BTnode(my_root,Node(1))
insert_BTnode(my_root,Node(6))