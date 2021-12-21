EnhancedMCTS:
	echo "#!/bin/bash" > EnhancedMCTS
	echo "java enhanced_mcts \"\$$@\"" >> EnhancedMCTS
	chmod u+x EnhancedMCTS