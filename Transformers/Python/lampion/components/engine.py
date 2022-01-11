"""
Contains the class "Engine", performing the toplevel orchestration of the Transformers,
also generates the transformers from config
and provides a default config.
"""
import os.path
import random

import logging as log
from libcst import CSTNode

from lampion.transformers.addcomment import AddCommentTransformer
from lampion.transformers.addneutral import AddNeutralElementTransformer
from lampion.transformers.addvar import AddVariableTransformer
from lampion.transformers.basetransformer import BaseTransformer
from lampion.transformers.lambdaidentity import LambdaIdentityTransformer
from lampion.transformers.renameparam import RenameParameterTransformer
from lampion.transformers.renamevar import RenameVariableTransformer


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
        - output_dir : Path
            Where to write the AST after transformations, using the same structure as input_dir


        It takes a list of transformers and applies them to a given AST.
        In the end the altered programs are written to files.

        The default behaviour is to apply all available transformations evenly distributed.
        If others are wanted, provide a distribution-function.

        The primary method is "run" and has similar comments laying out what's happening.

        This Engine is intentionally separated from any CLI / call to be better testable.
    """

    def __init__(self, config: dict = None, output_dir: str = "./lampion_output"):
        log.debug("Creating Engine ...")
        self.__config = _default_config()

        if config is None or len(config) == 0:
            log.info("Received no Config for Engine - running with default values")
        else:
            log.info("Received a Config for Engine - overwriting default values for everything found")
            overwrite_config = _default_config()
            overwrite_config.update(config)
            self.__config = overwrite_config

        self.__output_dir = output_dir
        self.__transformers = _create_transformers(self.__config)

        log.info("Initiated Engine "
                 "writing output to %s with %i Transformers",self.__output_dir,len(self.__transformers))

    __output_dir: str = "./lampion_output"
    __successful_transformations: int = 0
    __failed_transformations: int = 0
    __config = {}
    __transformers: [BaseTransformer] = []

    def run(self, csts: [(str, CSTNode)]) -> [(str, CSTNode)]:
        """
        Primary Method of the Engine.
        Does the following in order:

        - 1. While transformations are left:
        - 1.1 Pick a cst
        - 1.2 Pick a transformer
        - 1.3 Apply the Transformer
        - 1.4 Iff Transformer worked, inc transformations
        - 1.5 repeat 1
        - 2. Write SQL Statement (Currently Pending)
        - 3. If Outputdir!=None: Write Files
        - 4. Return changed Nodes

        The initial CSTs should remain unchanged.

        :param csts:Either A Single LibCST Module (if Input was a single file)
                    wrapped in a list or a list of such Modules (if Input was a folder).
        :return: The changed CST(s)

        The Nodes could be read in here too, but I wanted to separate it for better testability.
        This way, the Node can be passed to the engine without IO and the return can be asserted in tests.
        Similiarly, the paths are passed around with the CST as I do not want to loose them,
        because we definitely loose the order of them. So I feared that a simple dictionary might reach it's
        limitations and introduce bugs. To avoid it, I have put it like this. But feel free to make it elegant.
        """
        log.info("Starting Engine")
        # This deep clone helps to make a copy of the CSTs, so that the input does not change by accident.
        altered_csts = [(path, node.deep_clone()) for (path, node) in csts]

        random.seed(self.__config["seed"])

        max_transformations = self.__config["transformations"]
        while self.__successful_transformations < max_transformations:
            # 1.1 pick a cst
            cst_index = random.randint(0, len(altered_csts) - 1)
            (running_path, running_cst) = altered_csts[cst_index]
            del altered_csts[cst_index]
            # 1.2 pick a transformer
            transformer = random.choice(self.__transformers)
            transformer.reset()
            # 1.3 apply the transformer
            changed_cst = transformer.apply(running_cst)

            if transformer.worked():
                log.debug("Transformer worked")
                self.__successful_transformations = self.__successful_transformations + 1
                transformer.postprocessing()
                altered_csts.append((running_path, changed_cst))
            else:
                log.debug("Transformer failed - retrying with another one")
                self.__failed_transformations = self.__failed_transformations + 1
                transformer.reset()
                # If the Transformer failed, re-add the unaltered CST
                altered_csts.append((running_path, running_cst))

        if self.__config["writeManifest"]:
            log.warning("Manifest is currently not enabled!")
            raise NotImplementedError()
        else:
            log.info("Manifest Writing was turned off in Configuration.")

        if self.__output_dir:
            log.info("Writing to Output to %s",self.__output_dir)
            self._output_to_files(altered_csts)

        return altered_csts

    def get_config(self) -> dict:
        """
        :return: the current config, should usually contain parts of the default config.
        """
        return self.__config

    def get_transformers(self) -> BaseTransformer:
        """
        :return: the registered transformers
        """
        return self.__transformers

    def _output_to_files(self, csts: ["CSTNode"]) -> None:
        for (p, cst) in csts:
            pp = os.path.join("./", self.__output_dir, p)
            log.debug("Writing %s to %s",p,pp)

            # Create the (all) folders before trying to make the file
            os.makedirs(os.path.dirname(pp), exist_ok=True)
            # Open the File as write, with overwriting existing content
            with open(pp, "w") as output_file:
                output_file.write(cst.code)
                output_file.close()


def _create_transformers(config: dict) -> [BaseTransformer]:
    """ Creates a set of transformers from a given configuration.

    :param config:
        The programs configuration, declaring which Transformers are built and how they are configured.
    :return:
        A list of created transformers, if they are configurable their configuration is also read from the config.
    """

    transformers = []
    if not config:
        raise ValueError("Received None as Configuration")
    if len(config) == 0:
        raise ValueError("Received Empty Configuration")

    if config["AddUnusedVariableTransformer"]:
        transformers.append(AddVariableTransformer(string_randomness=config["UnusedVariableStringRandomness"]))

    if config["AddCommentTransformer"]:
        transformers.append(AddCommentTransformer(string_randomness=config["AddCommentStringRandomness"]))

    if config["RenameVariableTransformer"]:
        transformers.append(RenameVariableTransformer(string_randomness=config["RenameParameterStringRandomness"]))

    if config["RenameParameterTransformer"]:
        transformers.append(RenameParameterTransformer(string_randomness=config["RenameVariableStringRandomness"]))

    if config["LambdaIdentityTransformer"]:
        transformers.append(LambdaIdentityTransformer())

    if config["AddNeutralElementTransformer"]:
        transformers.append(AddNeutralElementTransformer())

    return transformers


def _default_config() -> dict:
    """
    Creates a default configuration, used if none was provided or if the provided configuration did not cover all values.
    Please be careful with the spelling of the dictionary.

    :return: The default configuration of the program.
    """
    default_config = {}
    # Program Wide Attributes
    default_config["seed"] = 11
    default_config["transformations"] = 50
    default_config["transformationscope"] = "global"

    # Manifest Attributes (Currently Disabled)
    default_config["writeManifest"] = False

    # Transformer Related Attributes
    default_config["AddUnusedVariableTransformer"] = True
    default_config["UnusedVariableStringRandomness"] = "full"

    default_config["AddCommentTransformer"] = True
    default_config["AddCommentStringRandomness"] = "full"

    default_config["RenameParameterTransformer"] = True
    default_config["RenameParameterStringRandomness"] = "full"

    default_config["RenameVariableTransformer"] = True
    default_config["RenameVariableStringRandomness"] = "full"

    default_config["AddNeutralElementTransformer"] = True

    default_config["LambdaIdentityTransformer"] = True

    return default_config
