<template>
  <div class="risk-quiz-page" v-loading="loading">
    <div class="page-header">
      <h2>风险评估问卷</h2>
    </div>

    <el-card v-if="questions.length > 0">
      <el-steps :active="currentStep" finish-status="success" align-center style="margin-bottom: 32px">
        <el-step v-for="(q, i) in questions" :key="i" :title="'第' + (i + 1) + '题'" />
      </el-steps>

      <div class="question-content">
        <h3>{{ currentQuestion.question }}</h3>
        <el-radio-group v-model="answers[currentQuestion.questionId]" size="large" class="option-group">
          <el-radio
            v-for="option in currentQuestion.options"
            :key="option.key"
            :value="option.key"
            border
            class="option-item"
          >
            {{ option.key }}. {{ option.text }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="navigation-buttons">
        <el-button @click="prevStep" :disabled="currentStep === 0">上一题</el-button>
        <el-button v-if="currentStep < questions.length - 1" type="primary" @click="nextStep" :disabled="!answers[currentQuestion.questionId]">
          下一题
        </el-button>
        <el-button v-else type="primary" @click="submitQuiz" :disabled="!allAnswered" :loading="submitting">
          提交评估
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getQuizQuestions, submitQuiz as submitQuizApi } from '@/api/investment'
import type { QuizQuestion } from '@/types/investment'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const currentStep = ref(0)
const questions = ref<QuizQuestion[]>([])
const answers = reactive<Record<string, string>>({})

const currentQuestion = computed(() => questions.value[currentStep.value] || { questionId: '', question: '', options: [] })
const allAnswered = computed(() => questions.value.every(q => answers[q.questionId]))

onMounted(() => {
  fetchQuestions()
})

async function fetchQuestions() {
  loading.value = true
  try {
    const res = await getQuizQuestions()
    if (res.data.code === 200) {
      questions.value = res.data.data
    }
  } catch {
    ElMessage.error('加载问卷失败')
  } finally {
    loading.value = false
  }
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

function nextStep() {
  if (currentStep.value < questions.value.length - 1) currentStep.value++
}

async function submitQuiz() {
  submitting.value = true
  try {
    const answerList = questions.value.map(q => ({
      questionId: q.questionId,
      answer: answers[q.questionId]
    }))
    const res = await submitQuizApi({ answers: answerList })
    if (res.data.code === 200) {
      ElMessage.success('评估完成！')
      router.push('/investment')
    }
  } catch {
    ElMessage.error('提交评估失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.risk-quiz-page {
  max-width: 800px;
  margin: 0 auto;

  .page-header {
    margin-bottom: 20px;
    h2 { margin: 0; }
  }

  .question-content {
    padding: 20px 0;

    h3 {
      font-size: 18px;
      margin-bottom: 24px;
      text-align: center;
    }

    .option-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
      max-width: 500px;
      margin: 0 auto;

      .option-item {
        margin: 0;
        width: 100%;
      }
    }
  }

  .navigation-buttons {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 32px;
    padding-top: 20px;
    border-top: 1px solid #eee;
  }
}
</style>
