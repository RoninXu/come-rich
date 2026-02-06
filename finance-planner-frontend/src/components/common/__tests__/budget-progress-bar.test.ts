import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { NProgress } from "naive-ui";
import BudgetProgressBar from "@/components/common/BudgetProgressBar.vue";

describe("BudgetProgressBar", () => {
  it("calculates utilization percentage from spent and budget", () => {
    const wrapper = mount(BudgetProgressBar, {
      props: {
        budget: 2000,
        spent: 1000,
      },
      global: {
        components: { NProgress },
      },
    });

    expect(wrapper.text()).toContain("50.0%");
  });

  it("caps progress at 100%", () => {
    const wrapper = mount(BudgetProgressBar, {
      props: {
        budget: 1000,
        spent: 1500,
      },
      global: {
        components: { NProgress },
      },
    });

    expect(wrapper.text()).toContain("100.0%");
  });
});
