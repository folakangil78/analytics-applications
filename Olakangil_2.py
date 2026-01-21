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
            if root.left_child is None: #possible typo here that i edited to root.left_child because need to access possible left child
                # before its assoc branch
                root.left_child = node
            else:
                insert_BTnode(root.left_child, node)
        else:
            if root.right_child is None:
                root.right_child = node
            else:
                insert_BTnode(root.right_child, node)

#function for dfs approach - using in-order traversing method
def DFS_my_BST(root):
    current = root
    dfs_result = [] #stack for nodes in order as dfs iterates through file
    stack = [] #separate stack list to keep track of current order

    while stack or current:
        #goes to left node (left child) becuase in order traversal method goes from root to left to root to right
        while current:
            stack.append(current) #inserts node onto stack tracker
            current = current.left_child #iterates node by node after insertion

        #need to pop most recent node bc that's the last left-sided child so need to move through it to get back to root and to the right
        current = stack.pop()
        dfs_result.append(current.data) #appending popped data shows us which node to explore next of whether it has childs or not

        current = current.right_child #visiting right subtree as long as current is filled

    return dfs_result

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
result = DFS_my_BST(x)
print(result)

# just some sample code
my_root = Node(3)
insert_BTnode(my_root,Node(7))
insert_BTnode(my_root,Node(1))
insert_BTnode(my_root,Node(6))