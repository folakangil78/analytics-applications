from collections import defaultdict
from collections import deque

class Graph:
    def __init__(self):
        self.graph = defaultdict(list)

    def addEdge(self, u, v): # u is vertex 1 and v is vertex 2, insert as quotations for each airport
        self.graph[u].append(v)
        self.graph[v].append(u) # graph bidirectionality
        # function to create edge (flight) between two airports

    def display_graph(self):
        for airport_dest in self.graph: # accesses graph field of Graph class
            print(f"{airport_dest}: {self.graph[airport_dest]}") # printing out adjacency list for graph
    
graph_obj = Graph() # basically creating object instance of class above

# could have done with iteration through list as well for edge creation bw airports
graph_obj.addEdge('ORD', 'CVG')
graph_obj.addEdge('JFK', 'ORD') # switching order to indicate graph bidirectionality
graph_obj.addEdge('ORD', 'MSP') # exact order of BFS search depends on how neighbor/child nodes are stored in graph
graph_obj.addEdge('ORD', 'DEN') # for ex, specific bfs output of ORD neighbors will depend on order that neighbors are inserted into graph here, 
# but overall output of bfs will stay the same
graph_obj.addEdge('SFO', 'SAN')
graph_obj.addEdge('SAN', 'PHX')
graph_obj.addEdge('PHX', 'DEN')
graph_obj.addEdge('IAH', 'ATL')
graph_obj.addEdge('ATL', 'JFK')
graph_obj.addEdge('DEN', 'IAH')
graph_obj.addEdge('SEA', 'DEN')
graph_obj.addEdge('MSP', 'DEN')
graph_obj.addEdge('CVG', 'JFK')
graph_obj.addEdge('SEA', 'SFO')

graph_obj.display_graph()
print("hellow test code hello world")

def bfs_destinations(airport_graph, starting_node):
    already_visited = set()  # creates set to keep track of nodes already visited (set prevents duplicates)
    visiting_queue = deque([starting_node])  # need deque queue to hit nodes FIFO bfs style
    
    while visiting_queue: # (is empty or has space)
        current_node = visiting_queue.popleft()  # remove node at queue top once processed
        
        if current_node not in already_visited: # only appending if not visited already
            print(current_node) # printing now because want to ouput source (ORD) along with its neighbors bef moving to next level
            already_visited.add(current_node) # insert into set to signal visited
            
            for neighbor_connection in airport_graph[current_node]: # inserting unvisited nodes to queue to be added upon next iteration
                if neighbor_connection not in already_visited:
                    visiting_queue.append(neighbor_connection) # ensures that children within adjacency list in further levels are accounted

bfs_destinations(graph_obj.graph, 'ORD')
# impt note: passing in specific graph attribute from Graph class of created class instance: graph_obj for first param
# can't pass in graph_obj by itself bc it's too general of an object, fxn needs the graph adjacency list specifically
# graph_obj.Graph (name of class) incorrect syntax
