import random

from lampion.transformers.addvar import AddVariableTransformer
from lampion.transformers.basetransformer import BaseTransformer

import logging as log

class Engine:
    """ The primary Engine that runs all of the Transformers on a piece of Code.

        Attributes:

        - transformers : :class:[BaseTransformer]
            A list of the transformers that are ready to be used
        - failed_transformations : int
            Number of failed transformations, that is how often transformers were called that did not work.
        - successful_transformations: int
            How often transformers were called that worked - used for termination.
        - config : Dict
            The configuration
        - input_dir : Path
            Where to read in the AST using LibCST's methods
        - output_dir : Path
            Where to write the AST after transformations, using the same structure as input_dir



    It takes a list of transformers and applies them to a given AST.
    In the end, if wished the results are written to an SQL database (currently pending)
    and the altered programs are written to files.

    The default behaviour is to apply all available transformations evenly distributed.
    If others are wanted, provide a distribution-function.

    The primary method is "run" and has similar comments laying out what's happening.

    This Engine is intentionally separated from any CLI / call to be better testable.
    """

    def __init__(self, config,output_dir: str):
        log.debug("Creating Engine ...")
        self.transformers = _create_transformers(config)
        self.config = config
        self.output_dir = output_dir

        log.info(f"Initiated Engine "
                 f"writing output to {self.output_dir} with {len(self.transformers)} Transformers")

    input_dir: str = None
    output_dir: str = "./lampion_output"
    successful_transformations: int = 0
    failed_transformations: int = 0
    config = {}
    transformers: [BaseTransformer] = []


    def run(self,csts: ["Node"]) -> ["Node"]:
        """
        Primary Method of the Engine.
        Does the following in order:
        1. While transformations are left:
            1.1 Pick a cst
            1.2 Pick a transformer
            1.3 Apply the Transformer
            1.4 Iff Transformer worked, inc transformations
            1.5 repeat 1
        2. Write SQL Statement (Currently Pending)
        3. If Outputdir!=None: Write Files
        4. Return changed Nodes

        The initial CSTs should remain unchanged.

        :param cst: Either A Single LibCST Module (if Input was a single file)
                    wrapped in a list or a list of such Modules (if Input was a folder).
        :return: The changed CST(s)

        The Nodes could be read in here too, but I wanted to separate it for better testability.
        This way, the Node can be passed to the engine without IO and the return can be asserted in tests.
        """
        log.info("Starting Engine")
        altered_csts = csts

        #TODO: Read this from config
        max_transformations = 10
        while self.successful_transformations < max_transformations:
            cst_index = random.randint(0,len(altered_csts)-1)
            cst = csts[cst_index]
            del altered_csts[cst_index]

            transformer = random.choice(self.transformers)
            transformer.reset()
            changed_cst = cst.visit(transformer)
            if transformer.worked():
                log.debug("Transformer worked")
                self.successful_transformations = self.successful_transformations + 1
                transformer.postprocessing()
                altered_csts.append(changed_cst)
            else:
                log.debug("Transformer failed - retrying with another one")
                self.failed_transformations = self.failed_transformations +1
                transformer.reset()
                # If the Transformer failed, re-add the unaltered CST
                altered_csts.append(cst)

        if self.output_dir:
            log.info("Writing to Output is TBD")

        return altered_csts


def _create_transformers(config) -> [BaseTransformer]:
    """ Creates a set of transformers from a given configuration.

    :param config:
        The programs configuration, declaring which Transformers are built and how they are configured.
    :return:
        A list of created transformers, if they are configurable their configuration is also read from the config.
    """

    # TODO: Currently I only have one Transformer
    return [AddVariableTransformer()]
