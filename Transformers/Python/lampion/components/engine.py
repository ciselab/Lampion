"""
Contains the class "Engine", performing the toplevel orchestration of the Transformers,
also generates the transformers from config
and provides a default config.
"""
import os.path
import random

import logging as log
import sys

from libcst import CSTNode

from lampion.transformers.addcomment import AddCommentTransformer
from lampion.transformers.addneutral import AddNeutralElementTransformer
from lampion.transformers.addvar import AddVariableTransformer
from lampion.transformers.basetransformer import BaseTransformer
from lampion.transformers.iffalseelse import IfFalseElseTransformer
from lampion.transformers.iftrue import IfTrueTransformer
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

    def __init__(self, config: dict = None, output_dir: str = "./lampion_output", store_only_changed: bool = False):
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
        self.__store_only_changed = store_only_changed

        self.__touched_files: {str} = set()
        self.__successful_transformations: int = 0
        self.__failed_transformations: int = 0

        log.info("Initiated Engine; "
                 "writing output to %s with %d Transformers", self.__output_dir, len(self.__transformers))

    def run(self, csts: [(str, CSTNode)]) -> [(str, CSTNode)]:
        """
        Primary Method of the Engine.
        Does the following in order:

        - 1. While transformations are left:
        - 1.1 Pick a cst
        - 1.2 Pick a transformer
        - 1.3 Apply the Transformer
        - 1.4 Iff Transformer worked, inc transformations, store file touched
        - 1.5 repeat 1
        - 2. If Outputdir!=None: Write Files
        - 3. Return changed Nodes

        The initial CSTs should remain unchanged, as copies are made.

        :param csts:Either A Single LibCST Module (if Input was a single file)
                    wrapped in a list *or* a list of such Modules (if Input was a folder).
        :return: The changed CST(s)

        The Nodes could be read in here too, but I wanted to separate it for better testability.
        This way, the Node can be passed to the engine without IO and the return can be asserted in tests.
        Similarly, the paths are passed around with the CST as I do not want to loose them,
        because we definitely loose the order of them. So I feared that a simple dictionary might reach it's
        limitations and introduce bugs. To avoid it, I have put it like this. But feel free to make it elegant.
        """
        log.info("Starting Engine")
        # This deep clone helps to make a copy of the CSTs, so that the input does not change by accident.
        altered_csts = [(path, node.deep_clone()) for (path, node) in csts]
        random.seed(self.__config["seed"])

        if self.__config["transformationscope"] == "global":
            altered_csts = self._run_transformations_global(altered_csts)
        # The "per_class" is a bit of a legacy writing, and will be removed soon!
        elif self.__config["transformationscope"] == "perClassEach" or self.__config["transformationscope"] == "per_class":
            altered_csts = self._run_transformations_per_class(altered_csts)
        else:
            log.error("Did not receive valid scope! Supported Scopes are: 'global','perClassEach'. Exiting early.")
            return altered_csts

        if self.__output_dir:
            log.info("Writing Output to %s", self.__output_dir)
            self._output_to_files(altered_csts)

        return altered_csts

    def _run_transformations_global(self, csts: [(str, CSTNode)]) -> [(str, CSTNode)]:
        """
        This method applied random transformers on a "global" scope.
        The cst to alter is chosen randomly from all available csts,
        but does not follow any other restrictions.
        This means, it can happen that some csts are changed multiple times while others are untouched.
        The total number of transformations is equal to __config["transformations"].

        :param csts: The csts to which to apply random transformers.
        :returns csts: The altered csts

        Required hidden variables:
        __config["transformations"]
        """
        max_transformations = self.__config["transformations"]
        while self.get_successful_transformations() < max_transformations:
            if self.get_failed_transformations() > max_transformations * 3:
                log.warning("Reached %d Transformation-Failures (3 times wanted transformations), exiting early",
                            self.get_failed_transformations())
                return csts
            # 1.1 pick a cst
            cst_index = random.randint(0, len(csts) - 1)
            (running_path, running_cst) = csts[cst_index]
            del csts[cst_index]
            # 1.2 pick a transformer
            transformer = random.choice(self.__transformers)
            transformer.reset()
            # 1.3 apply the transformer
            changed_cst = transformer.apply(running_cst)

            if transformer.worked():
                self._increase_success()
                transformer.postprocessing()
                self._touch(running_path)
                csts.append((running_path, changed_cst))
            else:
                self._increase_failure()
                transformer.reset()
                # If the Transformer failed, re-add the unaltered CST
                csts.append((running_path, running_cst))
            # Print some progress over the run of the engine
            if self.get_successful_transformations() % 1000 == 0 and self.get_successful_transformations() > 0:
                log.info("Finished %d Transformations", self.get_successful_transformations())
                log.debug("Currently failed Transformations: %d", self.get_failed_transformations())

        return csts

    def _run_transformations_per_class(self, csts: [(str, CSTNode)]) -> [(str, CSTNode)]:
        """
        This method applied random transformers on a "per_class" scope.
        Every CST will be touched with (exactly) __config["transformations"] transformers.
        The total number of transformations is equal to (csts * __config["transformations"]).
        Hence, recommended sizes for transformations are ~5-20, do not put as big numbers in as for "global".


        :param csts: The csts to which to apply random transformers.
        :returns csts: The altered csts

        The running variables for failing and successful transformations are not used like for global application,
        but I keep them for similar logging and printing progress.

        Required hidden variables:
        __config["transformations"]
        """
        if self.__config["transformations"] > 20:
            log.warning("Received a very high number of transformations for per_class mode (%d)!",
                        self.__config["transformations"])
        log.info("Running in per_class-mode for %d csts, total of %d transformations to be done", len(csts),
                 (len(csts) * self.__config["transformations"]))

        altered_csts = []
        for (running_path, running_cst) in csts:
            changed_cst = running_cst.deep_clone()
            successes: int = 0
            failures: int = 0
            while successes < self.__config["transformations"]:
                # Stopping in case of persistent error - three times as much as to be done is failure for this entry
                if failures > self.__config["transformations"] * 3:
                    log.warning("Failed %d attempts to alter %s after successfully doing %d transformations. "
                                "Continuing with next cst.", failures, running_path, successes)
                    successes = sys.maxsize
                    continue
                transformer = random.choice(self.__transformers)
                transformer.reset()
                # 1.3 apply the transformer
                changed_cst = transformer.apply(changed_cst)

                if transformer.worked():
                    self._increase_success()
                    transformer.postprocessing()
                    self._touch(running_path)
                    successes += 1
                else:
                    self._increase_failure()
                    failures += 1
                    transformer.reset()

                # Print some progress over the engine run
                if self.get_successful_transformations() % 1000 == 0 and self.get_successful_transformations() > 0:
                    log.info("Finished %d Transformations", self.get_successful_transformations())
                    log.debug("Currently failed Transformations: %d", self.get_failed_transformations())
            # After doing the inner loop, add the cst back
            altered_csts.append((running_path, changed_cst))

        return altered_csts

    def get_successful_transformations(self) -> int:
        """
        :return: the number of successful transformations, zero if not run yet.
        """
        return self.__successful_transformations

    def _increase_success(self) -> None:
        """
        Increases the number of successful transformations by one
        """
        self.__successful_transformations = self.__successful_transformations + 1

    def get_failed_transformations(self) -> int:
        """
        :return: the number of failed transformations
        """
        return self.__failed_transformations

    def _increase_failure(self) -> None:
        """
        Increases the number of failed transformations by one
        """
        self.__failed_transformations = self.__failed_transformations + 1

    def get_config(self) -> dict:
        """
        :return: the current config, should usually contain parts of the default config.
        """
        return self.__config

    def get_transformers(self) -> list[BaseTransformer]:
        """
        :return: the registered transformers
        """
        return self.__transformers

    def get_touched_paths(self) -> {str}:
        """
        :return: the touched files, empty if the engine did not run yet.
        """
        return self.__touched_files

    def _touch(self,path: str) -> None:
        """
        Adds a path to the touched paths.
        """
        self.__touched_files.add(path)

    def _output_to_files(self, csts: [(str, "CSTNode")]) -> None:
        log.info("Starting to write %d files to %s", len(csts), self.__output_dir)
        if self.__store_only_changed:
            log.info("Only writing files that were touched (%d files) ! ", len(self.__touched_files))
        else:
            log.info("Writing all files, even those unchanged. %d files changed", len(self.__touched_files))
        for (p, cst) in csts:
            # Check for condition:
            # Either We "just write stored" and the path must be touched
            # Or: Write all
            if not self.__store_only_changed or p in self.__touched_files:
                # if the path starts absolute, os.path.join doesn't like it. Cut the leading /
                inp_p = p if not p.startswith("/") else p[1:]
                pp = os.path.join(self.__output_dir, inp_p)
                if not (pp.startswith("/") or pp.startswith("./")):
                    pp = os.path.join("./", pp)
                log.debug("Writing %s to %s", p, pp)

                # Create the (required) folders before trying to make the file
                os.makedirs(os.path.dirname(pp), exist_ok=True)
                # Open the File as write, with overwriting existing content
                with open(pp, "w", encoding="utf-8") as output_file:
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

    if config["IfTrueTransformer"]:
        transformers.append(IfTrueTransformer())

    if config["IfFalseElseTransformer"]:
        transformers.append(IfFalseElseTransformer())

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
    # Supported are "global" and "perClassEach" (Spelling is important!)
    default_config["transformationscope"] = "global"

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

    default_config["IfTrueTransformer"] = True
    default_config["IfFalseElseTransformer"] = True

    return default_config
