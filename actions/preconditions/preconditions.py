class Preconditions:
    """
    A class for validating arguments provided to a class or method.
    """

    @staticmethod
    def check_not_none(reference, error_message="Argument must not be None"):
        """
        Checks that the provided reference is not None.

        :param reference: the reference to check
        :param error_message: the error message to raise in case of None
        :raises ValueError: if the reference is None
        """
        if reference is None:
            raise ValueError(error_message)
        return reference

    @staticmethod
    def check_argument(expression, error_message="Argument condition not met"):
        """
        Checks that the provided expression is True.

        :param expression: the expression to check
        :param error_message: the error message to raise in case of False
        :raises ValueError: if the expression is False
        """
        if not expression:
            raise ValueError(error_message)

    @staticmethod
    def check_state(expression, error_message="State condition not met"):
        """
        Checks that the provided expression is True for state validation.

        :param expression: the expression to check
        :param error_message: the error message to raise in case of False
        :raises ValueError: if the expression is False
        """
        if not expression:
            raise ValueError(error_message)
