<template>
  <div class="risk-quiz-page">
    <n-spin :show="loading">
      <PageHeader title="风险评估问卷" show-back />

      <GlassCard v-if="questions.length > 0">
        <n-steps :current="currentStep + 1" style="margin-bottom: 32px">
          <n-step v-for="(q, i) in questions" :key="i" :title="'第' + (i + 1) + '题'" />
        </n-steps>

        <div class="question-content">
          <h3>{{ currentQuestion.question }}</h3>
          <n-radio-group v-model:value="answers[currentQuestion.questionId]" class="option-group">
            <n-space vertical :size="12">
              <n-radio
                v-for="option in currentQuestion.options"
                :key="option.key"
                :value="option.key"
                class="option-item"
              >
                {{ option.key }}. {{ option.text }}
              </n-radio>
            </n-space>
          </n-radio-group>
        </div>

        <div class="navigation-buttons">
          <n-button @click="prevStep" :disabled="currentStep === 0">上一题</n-button>
          <n-button v-if="currentStep < questions.length - 1" type="primary" @click="nextStep" :disabled="!answers[currentQuestion.questionId]">
            下一题
          </n-button>
          <n-button v-else type="primary" @click="submitQuiz" :disabled="!allAnswered" :loading="submitting">
            提交评估
          </n-button>
        </div>
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NSteps, NStep, NRadioGroup, NRadio, NButton, NSpace, useMessage } from 'naive-ui'
import { getQuizQuestions, submitQuiz as submitQuizApi } from '@/api/investment'
import type { QuizQuestion } from '@/types/investment'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const currentStep = ref(0)
const questions = ref<QuizQuestion[]>([])
const answers = reactive<Record<string, string>>({})

const currentQuestion = computed(() => questions.value[currentStep.value] || { questionId: '', question: '', options: [] })
const allAnswered = computed(() => questions.value.every(q => answers[q.questionId]))

onMounted(() => { fetchQuestions() })

async function fetchQuestions() {
  loading.value = true
  try {
    const res = await getQuizQuestions()
    if (res.data.code === 200) questions.value = res.data.data
  } catch { message.error('加载问卷失败') }
  finally { loading.value = false }
}

function prevStep() { if (currentStep.value > 0) currentStep.value-- }
function nextStep() { if (currentStep.value < questions.value.length - 1) currentStep.value++ }

async function submitQuiz() {
  submitting.value = true
  try {
    const answerList = questions.value.map(q => ({ questionId: q.questionId, answer: answers[q.questionId] }))
    const res = await submitQuizApi({ answers: answerList })
    if (res.data.code === 200) { message.success('评估完成！'); router.push('/investment') }
  } catch { message.error('提交评估失败') }
  finally { submitting.value = false }
}
</script>

<style scoped lang="scss">
.risk-quiz-page {
  max-width: 800px;
  margin: 0 auto;

  .question-content {
    padding: 20px 0;

    h3 {
      font-size: 18px;
      margin-bottom: 24px;
      text-align: center;
      color: var(--cr-text-primary);
    }

    .option-group {
      max-width: 500px;
      margin: 0 auto;
      display: block;

      .option-item {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid var(--cr-border);
        border-radius: var(--cr-radius-md);
        transition: border-color 0.2s;

        &:hover { border-color: var(--cr-primary); }
      }
    }
  }

  .navigation-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 20px;
    border-top: 1px solid var(--cr-divider);
  }
}
</style>
