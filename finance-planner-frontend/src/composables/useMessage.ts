import {
  useMessage as useNaiveMessage,
  useDialog as useNaiveDialog,
} from "naive-ui";

/**
 * Wrapper composable that provides Naive UI message & dialog APIs.
 * Must be called inside setup() within the Naive UI provider tree.
 */
export function useAppMessage() {
  const message = useNaiveMessage();
  const dialog = useNaiveDialog();

  return { message, dialog };
}
