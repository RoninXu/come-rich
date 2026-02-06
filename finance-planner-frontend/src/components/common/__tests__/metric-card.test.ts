import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import MetricCard from "@/components/common/MetricCard.vue";

describe("MetricCard", () => {
  it("renders label and value", () => {
    const wrapper = mount(MetricCard, {
      props: {
        label: "本月收入",
        value: "¥12,000.00",
      },
    });

    expect(wrapper.text()).toContain("本月收入");
    expect(wrapper.text()).toContain("¥12,000.00");
  });

  it("shows formatted delta", () => {
    const wrapper = mount(MetricCard, {
      props: {
        label: "变化",
        value: "10%",
        delta: 8.28,
      },
    });

    expect(wrapper.text()).toContain("+8.3%");
  });
});
