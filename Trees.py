class Node:
    def __init__(self,key): #initialize fxn creating params for node and child
        self.left = None
        self.right = None
        self.val = key

        root = Node(0) #conveys 0 node as root of adjacency
        root.left = Node(1) 
        root.left.left = Node(3) #creates node connecting to 1 as left connection, could've done right

        root.right = Node(2)
        root.right.right = Node(5)
        root.right.left = Node(4) #important to keep ..right and ..left consistent because representing branches of node 2

#iterating through nodes, printing both nodes and connected edges
def iterate_thru_nodes(node_list, edge_list):
    tree_dict = {node: set() for node in node_list} #creates dict by converting each node to set (interpreted for loop)
    # setting each node as a set() creates empty set without duplicates for each node (credit Dr. Kula) 
    # before inserting child (second component of tuple)

    for edge in edge_list: #adding each child from tuple to parent node (which acts as key in dict)
        parent, child = edge #takes edges (all tuples) and sets first component to parent and second to child (initialized outside of fxn)
        tree_dict[parent].add(child) #inserts child as value to parent key in dict
    
    return tree_dict


nodes = [0, 1, 2, 3, 4, 5] #separate nodes
edges = [(0, 1), (0, 2), (1, 3), (2, 4), (2, 5)] #tuples representing node connecting to separate child (connection = edge)
print(iterate_thru_nodes(nodes, edges))